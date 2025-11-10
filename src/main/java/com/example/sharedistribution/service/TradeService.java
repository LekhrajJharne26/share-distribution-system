package com.example.sharedistribution.service;

import com.example.sharedistribution.dto.DistributionLine;
import com.example.sharedistribution.dto.TradeRequest;
import com.example.sharedistribution.dto.TradeResponse;
import com.example.sharedistribution.entity.*;
import com.example.sharedistribution.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class TradeService {

    private final ParticipantRepository participantRepo;
    private final HierarchyLinkRepository linkRepo;
    private final ShareConfigRepository shareRepo;
    private final TradeRepository tradeRepo;
    private final TradeDistributionRepository distributionRepo;

    public TradeService(ParticipantRepository participantRepo,
                        HierarchyLinkRepository linkRepo,
                        ShareConfigRepository shareRepo,
                        TradeRepository tradeRepo,
                        TradeDistributionRepository distributionRepo) {
        this.participantRepo = participantRepo;
        this.linkRepo = linkRepo;
        this.shareRepo = shareRepo;
        this.tradeRepo = tradeRepo;
        this.distributionRepo = distributionRepo;
    }

    // scale and rounding used for money calculations
    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    @Transactional
    public TradeResponse executeTrade(TradeRequest req) {
        // validate input
        if (req == null || req.getCustomerId() == null || req.getAmount() == null || req.getType() == null) {
            throw new IllegalArgumentException("Invalid trade request");
        }
        BigDecimal amount = req.getAmount().setScale(SCALE, RM);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be > 0");
        }

        Participant customer = participantRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // build chain from customer up to top owner
        List<Participant> chain = buildAncestorChain(customer);

        if (chain.size() == 0) {
            throw new IllegalStateException("Customer has no hierarchy configured");
        }

        // create trade record
        Trade trade = new Trade(customer, amount, req.getType());
        trade = tradeRepo.save(trade);

        List<DistributionLine> lines = new ArrayList<>();

        if (req.getType() == TradeType.LOSS) {
            // child -> parent flow: start at customer and go up
            BigDecimal current = amount;
            for (int i = 0; i < chain.size() - 1; i++) {
                Participant child = chain.get(i);
                Participant parent = chain.get(i + 1);

                ShareConfig cfg = shareRepo.findTopByParentAndChildOrderByUpdatedAtDesc(parent, child)
                        .orElseThrow(() -> new IllegalStateException(
                                "ShareConfig missing for parent=" + parent.getId() + " child=" + child.getId()));

                BigDecimal passPct = cfg.getPassPercentage().setScale(2, RM);
                BigDecimal amountPassed = current.multiply(passPct).divide(BigDecimal.valueOf(100), SCALE, RM);
                BigDecimal amountKept = current.subtract(amountPassed).setScale(SCALE, RM);

                // persist distribution for child
                distributionRepo.save(new TradeDistribution(trade, child, amountKept, amountPassed));

                lines.add(new DistributionLine(child.getId(), child.getName(), amountKept, amountPassed));

                current = amountPassed; // parent will process this in next iteration
            }
            // top participant keeps the remaining
            Participant top = chain.get(chain.size() - 1);
            BigDecimal topKept = current.setScale(SCALE, RM);
            distributionRepo.save(new TradeDistribution(trade, top, topKept, BigDecimal.ZERO));
            lines.add(new DistributionLine(top.getId(), top.getName(), topKept, BigDecimal.ZERO));
        } else {
            // PROFIT: parent -> child flow: start at top and go down
            List<Participant> reverse = new ArrayList<>(chain);
            Collections.reverse(reverse);
            BigDecimal current = amount;
            for (int i = 0; i < reverse.size() - 1; i++) {
                Participant parent = reverse.get(i);
                Participant child = reverse.get(i + 1);

                ShareConfig cfg = shareRepo.findTopByParentAndChildOrderByUpdatedAtDesc(parent, child)
                        .orElseThrow(() -> new IllegalStateException(
                                "ShareConfig missing for parent=" + parent.getId() + " child=" + child.getId()));

                BigDecimal passPct = cfg.getPassPercentage().setScale(2, RM);
                BigDecimal amountPassed = current.multiply(passPct).divide(BigDecimal.valueOf(100), SCALE, RM);
                BigDecimal amountKept = current.subtract(amountPassed).setScale(SCALE, RM);

                distributionRepo.save(new TradeDistribution(trade, parent, amountKept, amountPassed));
                lines.add(new DistributionLine(parent.getId(), parent.getName(), amountKept, amountPassed));

                current = amountPassed;
            }
            // bottom customer receives remaining
            Participant bottom = reverse.get(reverse.size() - 1);
            BigDecimal bottomKept = current.setScale(SCALE, RM);
            distributionRepo.save(new TradeDistribution(trade, bottom, bottomKept, BigDecimal.ZERO));
            lines.add(new DistributionLine(bottom.getId(), bottom.getName(), bottomKept, BigDecimal.ZERO));
        }

        return new TradeResponse(trade.getId(), trade.getAmount(), trade.getType(), lines);
    }

    /**
     * Build ancestor chain from customer up to top. The returned list is [customer, agent, operator, owner]
     * It will follow single parent link per node; if multiple parents found it throws an exception.
     */
    private List<Participant> buildAncestorChain(Participant customer) {
        List<Participant> chain = new ArrayList<>();
        Participant current = customer;
        chain.add(current);

        // iterate upward until no parent exists
        while (true) {
            List<HierarchyLink> parents = linkRepo.findByChild(current);
            if (parents == null || parents.isEmpty()) break;

            if (parents.size() > 1) {
                throw new IllegalStateException("Multiple parents configured for participant id=" + current.getId());
            }
            Participant parent = parents.get(0).getParent();
            chain.add(parent);
            current = parent;
        }
        return chain;
    }

    // -- Reporting helpers

    public List<DistributionLine> getTradeDistributions(Long tradeId) {
        Trade t = tradeRepo.findById(tradeId).orElseThrow(() -> new IllegalArgumentException("Trade not found"));
        List<TradeDistribution> rows = distributionRepo.findByTrade(t);
        return rows.stream()
                .map(r -> new DistributionLine(r.getParticipant().getId(), r.getParticipant().getName(),
                        r.getAmountKept(), r.getAmountPassed()))
                .collect(Collectors.toList());
    }
    public Map<Long, ParticipantSummary> dailySummary(LocalDate date) {
        // naive in-memory compute; fine for assignment/demo
        ZoneId ist = ZoneId.of("Asia/Kolkata");
        List<Trade> all = tradeRepo.findAll();

        Map<Long, ParticipantSummary> map = new HashMap<>();
        for (Trade t : all) {
            // convert trade createdAt (Instant) -> UTC zoned -> same instant in IST -> local date in IST
            LocalDate td = t.getCreatedAt().atZone(ZoneOffset.UTC).withZoneSameInstant(ist).toLocalDate();
            if (!td.equals(date)) continue;

            List<TradeDistribution> dlist = distributionRepo.findByTrade(t);
            for (TradeDistribution d : dlist) {
                Long pid = d.getParticipant().getId();
                ParticipantSummary s = map.computeIfAbsent(pid, k -> new ParticipantSummary(d.getParticipant().getName()));
                s.totalKept = s.totalKept.add(d.getAmountKept());
                s.totalPassed = s.totalPassed.add(d.getAmountPassed());
            }
        }
        return map;
    }


    public static class ParticipantSummary {
        public final String name;
        public BigDecimal totalKept = BigDecimal.ZERO.setScale(SCALE, RM);
        public BigDecimal totalPassed = BigDecimal.ZERO.setScale(SCALE, RM);
        public ParticipantSummary(String name) { this.name = name; }
    }
}

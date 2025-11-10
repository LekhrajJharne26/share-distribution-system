package com.example.sharedistribution;

import com.example.sharedistribution.entity.*;
import com.example.sharedistribution.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ParticipantRepository participantRepo;
    private final HierarchyLinkRepository linkRepo;
    private final ShareConfigRepository shareRepo;
    private final TradeRepository tradeRepo;
    private final TradeDistributionRepository distributionRepo;

    public DataInitializer(ParticipantRepository participantRepo,
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

    @Override
    public void run(String... args) throws Exception {
        // seed participants if none exist
        if (participantRepo.count() == 0) {
            Participant owner = participantRepo.save(new Participant("Owner A", ParticipantType.OWNER));
            Participant operator = participantRepo.save(new Participant("Operator A", ParticipantType.OPERATOR));
            Participant agent = participantRepo.save(new Participant("Agent A", ParticipantType.AGENT));
            Participant customer = participantRepo.save(new Participant("Customer A", ParticipantType.CUSTOMER));
            Participant customer2 = participantRepo.save(new Participant("Customer B", ParticipantType.CUSTOMER));

            // links: owner -> operator -> agent -> customer
            linkRepo.save(new HierarchyLink(owner, operator));
            linkRepo.save(new HierarchyLink(operator, agent));
            linkRepo.save(new HierarchyLink(agent, customer));
            linkRepo.save(new HierarchyLink(agent, customer2));

            // share configs: parent -> child
            shareRepo.save(new ShareConfig(owner, operator, BigDecimal.valueOf(90)));
            shareRepo.save(new ShareConfig(operator, agent, BigDecimal.valueOf(80)));
            shareRepo.save(new ShareConfig(agent, customer, BigDecimal.valueOf(80)));
            shareRepo.save(new ShareConfig(agent, customer2, BigDecimal.valueOf(80)));

            System.out.println("Seeded participants, links and share configs.");
        } else {
            System.out.println("Participants already present, skipping seed.");
        }

        // Optionally, create example trades if none exist
        if (tradeRepo.count() == 0) {
            // find a customer
            List<Participant> customers = participantRepo.findAll().stream()
                    .filter(p -> p.getType() == ParticipantType.CUSTOMER).toList();

            if (!customers.isEmpty()) {
                Participant c = customers.get(0);

                // Create a sample LOSS trade of 1000
                Trade t1 = tradeRepo.save(new Trade(c, BigDecimal.valueOf(1000.00), TradeType.LOSS));
                // no distribution rows here — the TradeService would normally create them when executing trades
                // but we insert a simple row to indicate sample trades exist
                System.out.println("Created sample trade (LOSS) id=" + t1.getId());

                // Create a sample PROFIT trade of 500.00
                Trade t2 = tradeRepo.save(new Trade(c, BigDecimal.valueOf(500.00), TradeType.PROFIT));
                System.out.println("Created sample trade (PROFIT) id=" + t2.getId());
            }
        }
    }
}

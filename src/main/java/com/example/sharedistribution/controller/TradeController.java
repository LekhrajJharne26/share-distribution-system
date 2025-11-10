package com.example.sharedistribution.controller;

import com.example.sharedistribution.dto.DistributionLine;
import com.example.sharedistribution.dto.TradeRequest;
import com.example.sharedistribution.dto.TradeResponse;
import com.example.sharedistribution.service.TradeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    /**
     * Execute a trade
     * POST /api/trades
     * Body: { "customerId": 10, "amount": 1000.0, "type": "LOSS" }
     */
    @PostMapping
    public ResponseEntity<?> execute(@Valid @RequestBody TradeRequest req) {
        try {
            TradeResponse resp = tradeService.executeTrade(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // Get distribution lines for a trade
    @GetMapping("/{id}")
    public ResponseEntity<?> getTrade(@PathVariable("id") Long id) {
        try {
            List<DistributionLine> lines = tradeService.getTradeDistributions(id);
            return ResponseEntity.ok(Map.of("tradeId", id, "distributions", lines));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Daily report: summary per participant
    @GetMapping("/reports/daily")
    public ResponseEntity<?> dailyReport(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Map<Long, TradeService.ParticipantSummary> map = tradeService.dailySummary(date);
            var out = map.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> Map.of("name", e.getValue().name,
                            "totalKept", e.getValue().totalKept,
                            "totalPassed", e.getValue().totalPassed)
            ));
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

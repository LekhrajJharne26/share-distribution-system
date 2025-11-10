package com.example.sharedistribution.dto;

import com.example.sharedistribution.entity.TradeType;

import java.math.BigDecimal;
import java.util.List;

public class TradeResponse {
    private Long tradeId;
    private BigDecimal amount;
    private TradeType type;
    private List<DistributionLine> distributions;

    public TradeResponse(Long tradeId, BigDecimal amount, TradeType type, List<DistributionLine> distributions) {
        this.tradeId = tradeId;
        this.amount = amount;
        this.type = type;
        this.distributions = distributions;
    }

    public Long getTradeId() { return tradeId; }
    public BigDecimal getAmount() { return amount; }
    public TradeType getType() { return type; }
    public List<DistributionLine> getDistributions() { return distributions; }
}

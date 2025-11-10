package com.example.sharedistribution.dto;

import com.example.sharedistribution.entity.TradeType;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

public class TradeRequest {
    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "type is required")
    private TradeType type;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public TradeType getType() { return type; }
    public void setType(TradeType type) { this.type = type; }
}

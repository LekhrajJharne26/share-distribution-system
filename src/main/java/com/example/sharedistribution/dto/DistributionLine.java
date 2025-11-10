package com.example.sharedistribution.dto;

import java.math.BigDecimal;

/**
 * DTO representing one participant's share for a trade.
 * Immutable (no setters) makes it safe to return from controllers.
 */
public class DistributionLine {
    private Long participantId;
    private String participantName;
    private BigDecimal amountKept;
    private BigDecimal amountPassed;

    public DistributionLine() {}

    public DistributionLine(Long participantId, String participantName, BigDecimal amountKept, BigDecimal amountPassed) {
        this.participantId = participantId;
        this.participantName = participantName;
        this.amountKept = amountKept;
        this.amountPassed = amountPassed;
    }

    public Long getParticipantId() { return participantId; }
    public String getParticipantName() { return participantName; }
    public BigDecimal getAmountKept() { return amountKept; }
    public BigDecimal getAmountPassed() { return amountPassed; }
}

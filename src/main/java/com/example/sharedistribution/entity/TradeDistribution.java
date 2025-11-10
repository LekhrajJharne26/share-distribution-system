package com.example.sharedistribution.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trade_distribution")
public class TradeDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private Trade trade;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    // how much this participant kept for themselves for this trade step
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountKept;

    // how much this participant passed on (upward or downward) for this trade step
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPassed;

    private Instant createdAt = Instant.now();

    public TradeDistribution() {}

    public TradeDistribution(Trade trade, Participant participant, BigDecimal amountKept, BigDecimal amountPassed) {
        this.trade = trade;
        this.participant = participant;
        this.amountKept = amountKept;
        this.amountPassed = amountPassed;
    }

    public Long getId() { return id; }
    public Trade getTrade() { return trade; }
    public Participant getParticipant() { return participant; }
    public BigDecimal getAmountKept() { return amountKept; }
    public BigDecimal getAmountPassed() { return amountPassed; }
    public Instant getCreatedAt() { return createdAt; }
}

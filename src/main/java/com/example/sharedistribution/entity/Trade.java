package com.example.sharedistribution.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trade")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // customer who initiated the trade
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Participant customer;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType type;

    private Instant createdAt = Instant.now();

    public Trade() {}

    public Trade(Participant customer, BigDecimal amount, TradeType type) {
        this.customer = customer;
        this.amount = amount;
        this.type = type;
    }

    public Long getId() { return id; }
    public Participant getCustomer() { return customer; }
    public BigDecimal getAmount() { return amount; }
    public TradeType getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
}

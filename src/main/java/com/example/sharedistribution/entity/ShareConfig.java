package com.example.sharedistribution.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "share_config")   // allow multiple rows per parent-child; pick latest at runtime
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class ShareConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // parent -> child config
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Participant parent;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private Participant child;

    // percentage to pass from parent -> child (0..100), stored as decimal (e.g., 90.00)
    @Column(name = "pass_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal passPercentage;

    private Instant updatedAt = Instant.now();

    public ShareConfig() {}

    public ShareConfig(Participant parent, Participant child, BigDecimal passPercentage) {
        this.parent = parent;
        this.child = child;
        this.passPercentage = passPercentage;
    }

    public Long getId() { return id; }
    public Participant getParent() { return parent; }
    public Participant getChild() { return child; }
    public BigDecimal getPassPercentage() { return passPercentage; }
    public void setPassPercentage(BigDecimal p) { this.passPercentage = p; this.updatedAt = Instant.now(); }
    public Instant getUpdatedAt() { return updatedAt; }
}

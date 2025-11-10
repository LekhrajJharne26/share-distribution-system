package com.example.sharedistribution.entity;

import jakarta.persistence.*;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "hierarchy_link", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"parent_id", "child_id"})
})
public class HierarchyLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Participant parent;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private Participant child;

    private Instant createdAt = Instant.now();

    public HierarchyLink() {}

    public HierarchyLink(Participant parent, Participant child) {
        this.parent = parent;
        this.child = child;
    }

    public Long getId() { return id; }
    public Participant getParent() { return parent; }
    public Participant getChild() { return child; }
    public Instant getCreatedAt() { return createdAt; }
}

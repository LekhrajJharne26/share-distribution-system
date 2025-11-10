package com.example.sharedistribution.entity;

import jakarta.persistence.*;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "participant")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantType type;

    private Instant createdAt = Instant.now();

    public Participant() {}

    public Participant(String name, ParticipantType type) {
        this.name = name;
        this.type = type;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ParticipantType getType() { return type; }
    public void setType(ParticipantType type) { this.type = type; }
    public Instant getCreatedAt() { return createdAt; }
}

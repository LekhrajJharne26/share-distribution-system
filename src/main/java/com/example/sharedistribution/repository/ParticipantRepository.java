package com.example.sharedistribution.repository;

import com.example.sharedistribution.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}

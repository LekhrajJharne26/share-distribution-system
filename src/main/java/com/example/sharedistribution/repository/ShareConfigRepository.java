package com.example.sharedistribution.repository;

import com.example.sharedistribution.entity.Participant;
import com.example.sharedistribution.entity.ShareConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShareConfigRepository extends JpaRepository<ShareConfig, Long> {
    Optional<ShareConfig> findByParentAndChild(Participant parent, Participant child);

    Optional<ShareConfig> findTopByParentAndChildOrderByUpdatedAtDesc(Participant parent, Participant child);

}

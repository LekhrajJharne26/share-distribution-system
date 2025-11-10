package com.example.sharedistribution.repository;

import com.example.sharedistribution.entity.HierarchyLink;
import com.example.sharedistribution.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HierarchyLinkRepository extends JpaRepository<HierarchyLink, Long> {
    List<HierarchyLink> findByChild(Participant child);
    List<HierarchyLink> findByParent(Participant parent);
}

package com.example.sharedistribution.controller;

import com.example.sharedistribution.entity.HierarchyLink;
import com.example.sharedistribution.entity.Participant;
import com.example.sharedistribution.entity.ParticipantType;
import com.example.sharedistribution.repository.HierarchyLinkRepository;
import com.example.sharedistribution.repository.ParticipantRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

    private final ParticipantRepository participantRepo;
    private final HierarchyLinkRepository linkRepo;

    public ParticipantController(ParticipantRepository participantRepo,
                                 HierarchyLinkRepository linkRepo) {
        this.participantRepo = participantRepo;
        this.linkRepo = linkRepo;
    }

    // Create participant
    @PostMapping
    public ResponseEntity<Participant> create(@Valid @RequestBody ParticipantRequest req) {
        if (req.name == null || req.name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Participant p = new Participant(req.name, ParticipantType.valueOf(req.type));
        Participant saved = participantRepo.save(p);
        return ResponseEntity.created(URI.create("/api/participants/" + saved.getId())).body(saved);
    }

    // Update participant (name or type)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @Valid @RequestBody ParticipantRequest req) {
        return participantRepo.findById(id).map(existing -> {
            if (req.name != null && !req.name.isBlank()) existing.setName(req.name);
            if (req.type != null && !req.type.isBlank()) {
                try {
                    ParticipantType pt = ParticipantType.valueOf(req.type.trim().toUpperCase());
                    existing.setType(pt);
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body(Map.of("error","Invalid type. Allowed: OWNER,OPERATOR,AGENT,CUSTOMER"));
                }
            }
            Participant saved = participantRepo.save(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }


    // List participants
    @GetMapping
    public ResponseEntity<List<Participant>> list() {
        return ResponseEntity.ok(participantRepo.findAll());
    }

    // Create parent-child link
    @PostMapping("/link")
    public ResponseEntity<HierarchyLink> link(@Valid @RequestBody LinkRequest req) {
        Optional<Participant> parentOpt = participantRepo.findById(req.parentId);
        Optional<Participant> childOpt = participantRepo.findById(req.childId);
        if (parentOpt.isEmpty() || childOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Participant parent = parentOpt.get();
        Participant child = childOpt.get();

        // basic validation: don't link same id
        if (parent.getId().equals(child.getId())) {
            return ResponseEntity.badRequest().build();
        }

        HierarchyLink link = new HierarchyLink(parent, child);
        HierarchyLink saved = linkRepo.save(link);
        return ResponseEntity.created(URI.create("/api/participants/link/" + saved.getId())).body(saved);
    }

    // Simple DTOs inside controller for brevity
    public static class ParticipantRequest {
        @NotBlank(message = "Name is required")
        public String name;

        @NotBlank(message = "Type is required (OWNER/OPERATOR/AGENT/CUSTOMER)")
        public String type;
    }

    public static class LinkRequest {
        @NotNull(message = "parentId is required")
        public Long parentId;

        @NotNull(message = "childId is required")
        public Long childId;
    }
}

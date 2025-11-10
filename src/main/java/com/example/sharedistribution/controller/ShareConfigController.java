package com.example.sharedistribution.controller;

import com.example.sharedistribution.entity.Participant;
import com.example.sharedistribution.entity.ShareConfig;
import com.example.sharedistribution.repository.ParticipantRepository;
import com.example.sharedistribution.repository.ShareConfigRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shares")
public class ShareConfigController {

    private final ShareConfigRepository shareRepo;
    private final ParticipantRepository participantRepo;

    public ShareConfigController(ShareConfigRepository shareRepo,
                                 ParticipantRepository participantRepo) {
        this.shareRepo = shareRepo;
        this.participantRepo = participantRepo;
    }

    /**
     * Create or update a share config between parent and child.
     * Body: { "parentId": 1, "childId": 2, "passPercentage": 90.0 }
     * If a config exists for parent->child it will be updated.
     */
    @PostMapping
    public ResponseEntity<ShareConfigResponse> upsert(@Valid @RequestBody ShareConfigRequest req) {
        if (req == null || req.parentId == null || req.childId == null || req.passPercentage == null) {
            return ResponseEntity.badRequest().build();
        }
        if (req.parentId.equals(req.childId)) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Participant> parentOpt = participantRepo.findById(req.parentId);
        Optional<Participant> childOpt = participantRepo.findById(req.childId);
        if (parentOpt.isEmpty() || childOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Participant parent = parentOpt.get();
        Participant child = childOpt.get();

        // validate passPercentage range (0..100)
        BigDecimal pct = req.passPercentage;
        if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(BigDecimal.valueOf(100)) > 0) {
            return ResponseEntity.badRequest().build();
        }

        Optional<ShareConfig> existing = shareRepo.findByParentAndChild(parent, child);

        ShareConfig saved;
        if (existing.isPresent()) {
            ShareConfig sc = existing.get();
            sc.setPassPercentage(pct);
            saved = shareRepo.save(sc);
        } else {
            ShareConfig sc = new ShareConfig(parent, child, pct);
            saved = shareRepo.save(sc);
        }

        ShareConfigResponse resp = new ShareConfigResponse(saved.getId(),
                saved.getParent().getId(), saved.getChild().getId(), saved.getPassPercentage());
        return ResponseEntity.created(URI.create("/api/shares/" + saved.getId())).body(resp);
    }

    // list all share configs
    @GetMapping
    public ResponseEntity<List<ShareConfig>> list() {
        return ResponseEntity.ok(shareRepo.findAll());
    }

    // lookup by parent & child
    @GetMapping("/lookup")
    public ResponseEntity<ShareConfigResponse> lookup(@RequestParam Long parentId, @RequestParam Long childId) {
        Optional<Participant> parentOpt = participantRepo.findById(parentId);
        Optional<Participant> childOpt = participantRepo.findById(childId);
        if (parentOpt.isEmpty() || childOpt.isEmpty()) return ResponseEntity.notFound().build();
        Optional<ShareConfig> sc = shareRepo.findByParentAndChild(parentOpt.get(), childOpt.get());
        return sc.map(s -> ResponseEntity.ok(
                        new ShareConfigResponse(s.getId(), s.getParent().getId(), s.getChild().getId(), s.getPassPercentage())))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- DTOs ---
    public static class ShareConfigRequest {
        @NotNull(message = "parentId is required")
        public Long parentId;

        @NotNull(message = "childId is required")
        public Long childId;

        @NotNull(message = "passPercentage is required")
        @DecimalMin(value = "0.0", message = "passPercentage must be >= 0")
        public BigDecimal passPercentage;
    }

    public static class ShareConfigResponse {
        public Long id;
        public Long parentId;
        public Long childId;
        public BigDecimal passPercentage;

        public ShareConfigResponse(Long id, Long parentId, Long childId, BigDecimal passPercentage) {
            this.id = id;
            this.parentId = parentId;
            this.childId = childId;
            this.passPercentage = passPercentage;
        }
    }
}

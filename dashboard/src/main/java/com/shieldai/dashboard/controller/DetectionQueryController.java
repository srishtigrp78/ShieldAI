package com.shieldai.dashboard.controller;

import com.shieldai.dashboard.entity.DetectionEntity;
import com.shieldai.dashboard.repository.DetectionRepository;
import com.shieldai.shared.DetectionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000") // dev-only; remove/adjust in prod
@RestController
@RequestMapping("/api/detections/query")
public class DetectionQueryController {
    private static final Logger log = LoggerFactory.getLogger(DetectionQueryController.class);
    private final DetectionRepository repo;

    public DetectionQueryController(DetectionRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<DetectionReport>> listDetections() {
        List<DetectionReport> out = repo.findAll().stream()
                .map(this::toReport)
                .collect(Collectors.toList());
        log.info("Returning {} detections", out.size());
        return ResponseEntity.ok(out);
    }

    // optional POST search endpoint at /api/detections/query
    @PostMapping
    public ResponseEntity<List<DetectionReport>> listDetectionsPost(@RequestBody(required = false) Map<String, Object> body) {
        try {
            log.info("Received detections POST (query), body={}", body);
            return ResponseEntity.ok(Collections.emptyList());
        } catch (Throwable t) {
            log.error("Unexpected error in detections query endpoint", t);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    private DetectionReport toReport(DetectionEntity e) {
        DetectionReport r = new DetectionReport();
        try {
            r.setCandidateId(e.getCandidateId());
        } catch (Throwable ignored) {}
        try { r.setToolName(e.getToolName()); } catch (Throwable ignored) {}
        try { r.setTimestamp(e.getTimestamp()); } catch (Throwable ignored) {}
        try { r.setProcessDetails(e.getProcessDetails()); } catch (Throwable ignored) {}
        try { r.setDescription(e.getOsInfo()); } catch (Throwable ignored) {}
        try { r.setConfidence(e.getConfidence() == null ? 0.0 : e.getConfidence()); } catch (Throwable ignored) {}
        return r;
    }
}
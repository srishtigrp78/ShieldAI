package com.shieldai.dashboard.controller;

import com.shieldai.dashboard.service.DetectionService;
import com.shieldai.dashboard.service.ExportService;
import com.shieldai.shared.DetectionReport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "http://localhost:3000")
public class ExportController {

    private final DetectionService detectionService;
    private final ExportService exportService;

    public ExportController(DetectionService detectionService, ExportService exportService) {
        this.detectionService = detectionService;
        this.exportService = exportService;
    }

    @GetMapping("/csv")
    public ResponseEntity<String> exportCSV(
            @RequestParam(required = false) String candidateId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String search
    ) {
        List<DetectionReport> detections = detectionService.getFilteredDetections(candidateId, startDate, endDate, toolName, search);
        String csv = exportService.generateCSV(detections);
        
        String filename = "detections_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPDF(
            @RequestParam(required = false) String candidateId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String search
    ) {
        List<DetectionReport> detections = detectionService.getFilteredDetections(candidateId, startDate, endDate, toolName, search);
        byte[] pdf = exportService.generatePDF(detections);
        
        String filename = "detections_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
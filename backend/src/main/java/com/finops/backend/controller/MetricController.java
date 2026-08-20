package com.finops.backend.controller;

import com.finops.backend.dto.CreateMetricRequest;
import com.finops.backend.dto.ResourceMetricResponse;
import com.finops.backend.service.MetricService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<ResourceMetricResponse> createMetric(
            Authentication authentication,
            @Valid @RequestBody CreateMetricRequest request) {
        String email = authentication.getName();
        ResourceMetricResponse response = metricService.createMetric(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<List<ResourceMetricResponse>> getMetrics(
            Authentication authentication,
            @RequestParam Integer resourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        String email = authentication.getName();
        List<ResourceMetricResponse> metrics = metricService.getMetricsForResource(email, resourceId, startTime, endTime);
        return ResponseEntity.ok(metrics);
    }
}

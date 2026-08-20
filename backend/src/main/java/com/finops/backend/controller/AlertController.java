package com.finops.backend.controller;

import com.finops.backend.dto.AlertResponse;
import com.finops.backend.dto.CreateAlertRequest;
import com.finops.backend.dto.UpdateAlertStatusRequest;
import com.finops.backend.entity.AlertStatus;
import com.finops.backend.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/detect")
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<List<AlertResponse>> detectAnomalies(
            Authentication authentication,
            @RequestParam(required = false) Integer accountId) {
        String email = authentication.getName();
        List<AlertResponse> alerts = alertService.detectAnomalies(email, accountId);
        return new ResponseEntity<>(alerts, HttpStatus.CREATED);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<AlertResponse> createAlert(
            Authentication authentication,
            @Valid @RequestBody CreateAlertRequest request) {
        String email = authentication.getName();
        AlertResponse response = alertService.createAlert(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<List<AlertResponse>> getAlerts(
            Authentication authentication,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) Integer resourceId) {
        String email = authentication.getName();
        List<AlertResponse> alerts = alertService.getAlerts(email, status, accountId, resourceId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        long count = alertService.getUnreadCount(email);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<AlertResponse> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAlertStatusRequest request) {
        String email = authentication.getName();
        AlertResponse response = alertService.updateStatus(email, id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}

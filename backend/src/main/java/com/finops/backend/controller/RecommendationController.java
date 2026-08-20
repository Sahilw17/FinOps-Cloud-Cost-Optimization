package com.finops.backend.controller;

import com.finops.backend.dto.CreateRecommendationRequest;
import com.finops.backend.dto.RecommendationResponse;
import com.finops.backend.dto.UpdateRecommendationStatusRequest;
import com.finops.backend.entity.RecommendationStatus;
import com.finops.backend.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<List<RecommendationResponse>> analyzeRecommendations(
            Authentication authentication,
            @RequestParam(required = false) Integer accountId) {
        String email = authentication.getName();
        List<RecommendationResponse> responses = recommendationService.analyzeAndGenerateRecommendations(email, accountId);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<RecommendationResponse> createRecommendation(
            Authentication authentication,
            @Valid @RequestBody CreateRecommendationRequest request) {
        String email = authentication.getName();
        RecommendationResponse response = recommendationService.createRecommendation(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(
            Authentication authentication,
            @RequestParam(required = false) RecommendationStatus status,
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) Integer resourceId) {
        String email = authentication.getName();
        List<RecommendationResponse> recommendations = recommendationService.getRecommendations(email, status, accountId, resourceId);
        return ResponseEntity.ok(recommendations);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<RecommendationResponse> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecommendationStatusRequest request) {
        String email = authentication.getName();
        RecommendationResponse response = recommendationService.updateStatus(email, id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}

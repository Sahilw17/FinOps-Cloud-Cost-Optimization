package com.finops.backend.controller;

import com.finops.backend.dto.CreatePredictionRequest;
import com.finops.backend.dto.GeneratePredictionRequest;
import com.finops.backend.dto.PredictionResponse;
import com.finops.backend.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<PredictionResponse> generatePrediction(
            Authentication authentication,
            @RequestBody GeneratePredictionRequest request) {
        String email = authentication.getName();
        PredictionResponse response = predictionService.generatePrediction(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<PredictionResponse> createPrediction(
            Authentication authentication,
            @Valid @RequestBody CreatePredictionRequest request) {
        String email = authentication.getName();
        PredictionResponse response = predictionService.createPrediction(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<List<PredictionResponse>> getPredictions(
            Authentication authentication,
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) Integer resourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String email = authentication.getName();
        List<PredictionResponse> predictions = predictionService.getPredictions(email, accountId, resourceId, startDate, endDate);
        return ResponseEntity.ok(predictions);
    }
}

package com.finops.backend.controller;

import com.finops.backend.dto.CostHistoryResponse;
import com.finops.backend.dto.CreateCostRequest;
import com.finops.backend.service.CostService;
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
@RequestMapping("/api/costs")
public class CostController {

    private final CostService costService;

    public CostController(CostService costService) {
        this.costService = costService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<CostHistoryResponse> createCost(
            Authentication authentication,
            @Valid @RequestBody CreateCostRequest request) {
        String email = authentication.getName();
        CostHistoryResponse response = costService.createCost(email, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
    public ResponseEntity<List<CostHistoryResponse>> getCosts(
            Authentication authentication,
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) Integer resourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String email = authentication.getName();
        List<CostHistoryResponse> costs = costService.getCostHistory(email, accountId, resourceId, startDate, endDate);
        return ResponseEntity.ok(costs);
    }
}

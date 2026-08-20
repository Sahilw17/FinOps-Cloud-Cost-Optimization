package com.finops.backend.controller;

import com.finops.backend.dto.AiAnalysisResponse;
import com.finops.backend.dto.AiChatRequest;
import com.finops.backend.dto.AiChatResponse;
import com.finops.backend.service.AiAdvisorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAnyRole('USER', 'FINOPS_ENGINEER', 'ADMIN')")
public class AiAdvisorController {

    private final AiAdvisorService aiAdvisorService;

    public AiAdvisorController(AiAdvisorService aiAdvisorService) {
        this.aiAdvisorService = aiAdvisorService;
    }

    @PostMapping("/analyze-resource/{resourceId}")
    public ResponseEntity<AiAnalysisResponse> analyzeResource(
            @PathVariable Integer resourceId,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(aiAdvisorService.analyzeResource(email, resourceId));
    }

    @PostMapping("/copilot/chat")
    public ResponseEntity<AiChatResponse> chatWithCopilot(
            @RequestBody AiChatRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(aiAdvisorService.chat(email, request));
    }
}

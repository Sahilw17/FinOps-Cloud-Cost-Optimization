package com.finops.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finops.backend.dto.AwsResourceResponse;
import com.finops.backend.dto.CreateAwsResourceRequest;
import com.finops.backend.service.AwsResourceService;

@RestController
@RequestMapping("/api/resources")
public class AwsResourceController {

    private final AwsResourceService awsResourceService;

    public AwsResourceController(AwsResourceService awsResourceService) {
        this.awsResourceService = awsResourceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FINOPS_ENGINEER')")
    public AwsResourceResponse createResource(
            Authentication authentication,
            @RequestBody CreateAwsResourceRequest request) {
        return awsResourceService.createResource(authentication.getName(), request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FINOPS_ENGINEER')")
    public List<AwsResourceResponse> getResources(Authentication authentication) {
        return awsResourceService.getResourcesByUser(authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'FINOPS_ENGINEER')")
    public AwsResourceResponse getResourceById(
            Authentication authentication,
            @PathVariable Integer id) {
        return awsResourceService.getResourceById(authentication.getName(), id);
    }
}

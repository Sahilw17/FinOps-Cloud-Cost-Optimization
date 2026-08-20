package com.finops.backend.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.finops.backend.dto.AwsAccountResponse;
import com.finops.backend.dto.CreateAwsAccountRequest;
import com.finops.backend.dto.UpdateAwsAccountStatusRequest;
import com.finops.backend.service.AwsAccountService;

@RestController
@RequestMapping("/api/accounts")
public class AwsAccountController {

    private final AwsAccountService awsAccountService;

    public AwsAccountController(AwsAccountService awsAccountService) {
        this.awsAccountService = awsAccountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AwsAccountResponse createAwsAccount(
            @Valid @RequestBody CreateAwsAccountRequest request,
            Authentication authentication) {
        return awsAccountService.createAwsAccount(authentication.getName(), request);
    }

    @GetMapping
    public List<AwsAccountResponse> getUserAwsAccounts(Authentication authentication) {
        return awsAccountService.getUserAwsAccounts(authentication.getName());
    }

    @GetMapping("/{id}")
    public AwsAccountResponse getAwsAccountById(
            @PathVariable Integer id,
            Authentication authentication) {
        return awsAccountService.getAwsAccountById(authentication.getName(), id);
    }

    @PutMapping("/{id}/status")
    public AwsAccountResponse updateAwsAccountStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateAwsAccountStatusRequest request,
            Authentication authentication) {
        return awsAccountService.updateAccountStatus(authentication.getName(), id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAwsAccount(
            @PathVariable Integer id,
            Authentication authentication) {
        awsAccountService.deleteAwsAccount(authentication.getName(), id);
    }
}

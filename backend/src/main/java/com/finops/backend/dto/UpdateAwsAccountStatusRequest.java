package com.finops.backend.dto;

import jakarta.validation.constraints.NotNull;

import com.finops.backend.entity.AccountStatus;

public class UpdateAwsAccountStatusRequest {

    @NotNull(message = "Status is required")
    private AccountStatus status;

    public UpdateAwsAccountStatusRequest() {}

    public UpdateAwsAccountStatusRequest(AccountStatus status) {
        this.status = status;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}

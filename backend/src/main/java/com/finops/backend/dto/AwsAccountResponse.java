package com.finops.backend.dto;

import java.time.LocalDateTime;

import com.finops.backend.entity.AccountStatus;

public class AwsAccountResponse {

    private Integer id;
    private String awsAccountNumber;
    private String accountName;
    private AccountStatus status;
    private String maskedAccessKey;
    private LocalDateTime createdAt;

    public AwsAccountResponse() {}

    public AwsAccountResponse(Integer id, String awsAccountNumber, String accountName, AccountStatus status, String maskedAccessKey, LocalDateTime createdAt) {
        this.id = id;
        this.awsAccountNumber = awsAccountNumber;
        this.accountName = accountName;
        this.status = status;
        this.maskedAccessKey = maskedAccessKey;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAwsAccountNumber() {
        return awsAccountNumber;
    }

    public void setAwsAccountNumber(String awsAccountNumber) {
        this.awsAccountNumber = awsAccountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getMaskedAccessKey() {
        return maskedAccessKey;
    }

    public void setMaskedAccessKey(String maskedAccessKey) {
        this.maskedAccessKey = maskedAccessKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

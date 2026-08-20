package com.finops.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateAwsAccountRequest {

    @NotBlank(message = "AWS Account Number is required")
    @Pattern(regexp = "^\\d{12}$", message = "AWS Account Number must be exactly 12 digits")
    private String awsAccountNumber;

    @NotBlank(message = "Account Name is required")
    @Size(max = 100, message = "Account Name must be less than 100 characters")
    private String accountName;

    @NotBlank(message = "Access Key is required")
    @Size(max = 100, message = "Access Key must be less than 100 characters")
    private String accessKey;

    @NotBlank(message = "Secret Key is required")
    @Size(max = 255, message = "Secret Key must be less than 255 characters")
    private String secretKey;

    public CreateAwsAccountRequest() {}

    public CreateAwsAccountRequest(String awsAccountNumber, String accountName, String accessKey, String secretKey) {
        this.awsAccountNumber = awsAccountNumber;
        this.accountName = accountName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
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

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}

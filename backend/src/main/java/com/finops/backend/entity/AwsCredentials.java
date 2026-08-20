package com.finops.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.finops.backend.config.SecretKeyConverter;

@Entity
@Table(name = "AWS_Credentials")
public class AwsCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credential_id")
    private Integer id;

    @Column(name = "access_key", nullable = false, length = 100)
    private String accessKey;

    @Convert(converter = SecretKeyConverter.class)
    @Column(name = "secret_key", nullable = false, length = 255)
    private String secretKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AwsCredentials() {}

    public AwsCredentials(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public Integer getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

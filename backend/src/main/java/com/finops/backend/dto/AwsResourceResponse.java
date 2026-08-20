package com.finops.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AwsResourceResponse {

    private Integer id;
    private String awsAccountNumber;
    private String regionCode;
    private String serviceName;
    private String resourceTypeName;
    private String stateName;
    private String resourceName;
    private String resourceArn;
    private LocalDateTime discoveredAt;
    private List<ResourceTagDto> tags;

    public AwsResourceResponse() {}

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

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public void setResourceTypeName(String resourceTypeName) {
        this.resourceTypeName = resourceTypeName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceArn() {
        return resourceArn;
    }

    public void setResourceArn(String resourceArn) {
        this.resourceArn = resourceArn;
    }

    public LocalDateTime getDiscoveredAt() {
        return discoveredAt;
    }

    public void setDiscoveredAt(LocalDateTime discoveredAt) {
        this.discoveredAt = discoveredAt;
    }

    public List<ResourceTagDto> getTags() {
        return tags;
    }

    public void setTags(List<ResourceTagDto> tags) {
        this.tags = tags;
    }
}

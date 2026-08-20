package com.finops.backend.dto;

import java.util.List;

public class CreateAwsResourceRequest {

    private Integer awsAccountId;
    private String regionCode;
    private String resourceTypeName;
    private String stateName;
    private String resourceName;
    private String resourceArn;
    private List<ResourceTagDto> tags;

    public CreateAwsResourceRequest() {}

    public Integer getAwsAccountId() {
        return awsAccountId;
    }

    public void setAwsAccountId(Integer awsAccountId) {
        this.awsAccountId = awsAccountId;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
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

    public List<ResourceTagDto> getTags() {
        return tags;
    }

    public void setTags(List<ResourceTagDto> tags) {
        this.tags = tags;
    }
}

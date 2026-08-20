package com.finops.backend.dto;

import java.util.List;

public class CatalogResponse {

    private List<RegionDto> regions;
    private List<AwsServiceDto> services;
    private List<ResourceTypeDto> resourceTypes;
    private List<ResourceStateDto> resourceStates;

    public CatalogResponse() {}

    public List<RegionDto> getRegions() {
        return regions;
    }

    public void setRegions(List<RegionDto> regions) {
        this.regions = regions;
    }

    public List<AwsServiceDto> getServices() {
        return services;
    }

    public void setServices(List<AwsServiceDto> services) {
        this.services = services;
    }

    public List<ResourceTypeDto> getResourceTypes() {
        return resourceTypes;
    }

    public void setResourceTypes(List<ResourceTypeDto> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    public List<ResourceStateDto> getResourceStates() {
        return resourceStates;
    }

    public void setResourceStates(List<ResourceStateDto> resourceStates) {
        this.resourceStates = resourceStates;
    }

    public static class RegionDto {
        private String regionCode;
        private String regionName;

        public RegionDto(String regionCode, String regionName) {
            this.regionCode = regionCode;
            this.regionName = regionName;
        }

        public String getRegionCode() { return regionCode; }
        public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
        public String getRegionName() { return regionName; }
        public void setRegionName(String regionName) { this.regionName = regionName; }
    }

    public static class AwsServiceDto {
        private String serviceName;
        private String namespace;

        public AwsServiceDto(String serviceName, String namespace) {
            this.serviceName = serviceName;
            this.namespace = namespace;
        }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getNamespace() { return namespace; }
        public void setNamespace(String namespace) { this.namespace = namespace; }
    }

    public static class ResourceTypeDto {
        private String resourceTypeName;
        private String serviceName;

        public ResourceTypeDto(String resourceTypeName, String serviceName) {
            this.resourceTypeName = resourceTypeName;
            this.serviceName = serviceName;
        }

        public String getResourceTypeName() { return resourceTypeName; }
        public void setResourceTypeName(String resourceTypeName) { this.resourceTypeName = resourceTypeName; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    }

    public static class ResourceStateDto {
        private String stateName;
        private String resourceTypeName;

        public ResourceStateDto(String stateName, String resourceTypeName) {
            this.stateName = stateName;
            this.resourceTypeName = resourceTypeName;
        }

        public String getStateName() { return stateName; }
        public void setStateName(String stateName) { this.stateName = stateName; }
        public String getResourceTypeName() { return resourceTypeName; }
        public void setResourceTypeName(String resourceTypeName) { this.resourceTypeName = resourceTypeName; }
    }
}

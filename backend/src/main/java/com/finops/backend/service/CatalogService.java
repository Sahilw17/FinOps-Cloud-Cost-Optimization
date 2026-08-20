package com.finops.backend.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finops.backend.dto.CatalogResponse;
import com.finops.backend.repository.AwsServiceRepository;
import com.finops.backend.repository.RegionRepository;
import com.finops.backend.repository.ResourceStateRepository;
import com.finops.backend.repository.ResourceTypeRepository;

@Service
public class CatalogService {

    private final ObjectProvider<RegionRepository> regionRepositoryProvider;
    private final ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider;
    private final ObjectProvider<ResourceTypeRepository> resourceTypeRepositoryProvider;
    private final ObjectProvider<ResourceStateRepository> resourceStateRepositoryProvider;

    public CatalogService(
            ObjectProvider<RegionRepository> regionRepositoryProvider,
            ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider,
            ObjectProvider<ResourceTypeRepository> resourceTypeRepositoryProvider,
            ObjectProvider<ResourceStateRepository> resourceStateRepositoryProvider) {
        this.regionRepositoryProvider = regionRepositoryProvider;
        this.awsServiceRepositoryProvider = awsServiceRepositoryProvider;
        this.resourceTypeRepositoryProvider = resourceTypeRepositoryProvider;
        this.resourceStateRepositoryProvider = resourceStateRepositoryProvider;
    }

    @Transactional(readOnly = true)
    public CatalogResponse getCatalog() {
        CatalogResponse response = new CatalogResponse();

        RegionRepository regionRepo = regionRepositoryProvider.getIfAvailable();
        AwsServiceRepository serviceRepo = awsServiceRepositoryProvider.getIfAvailable();
        ResourceTypeRepository typeRepo = resourceTypeRepositoryProvider.getIfAvailable();
        ResourceStateRepository stateRepo = resourceStateRepositoryProvider.getIfAvailable();

        if (regionRepo != null) {
            response.setRegions(regionRepo.findAll().stream()
                    .map(r -> new CatalogResponse.RegionDto(r.getRegionCode(), r.getRegionName()))
                    .collect(Collectors.toList()));
        }

        if (serviceRepo != null) {
            response.setServices(serviceRepo.findAll().stream()
                    .map(s -> new CatalogResponse.AwsServiceDto(s.getServiceName(), s.getNamespace()))
                    .collect(Collectors.toList()));
        }

        if (typeRepo != null) {
            response.setResourceTypes(typeRepo.findAll().stream()
                    .map(t -> new CatalogResponse.ResourceTypeDto(t.getResourceTypeName(), t.getService().getServiceName()))
                    .collect(Collectors.toList()));
        }

        if (stateRepo != null) {
            response.setResourceStates(stateRepo.findAll().stream()
                    .map(s -> new CatalogResponse.ResourceStateDto(s.getStateName(), s.getResourceType().getResourceTypeName()))
                    .collect(Collectors.toList()));
        }

        return response;
    }
}

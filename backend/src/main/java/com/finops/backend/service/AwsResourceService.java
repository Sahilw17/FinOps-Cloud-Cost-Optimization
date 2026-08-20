package com.finops.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finops.backend.dto.AwsResourceResponse;
import com.finops.backend.dto.CreateAwsResourceRequest;
import com.finops.backend.dto.ResourceTagDto;
import com.finops.backend.entity.AwsAccount;
import com.finops.backend.entity.AwsResource;
import com.finops.backend.entity.Region;
import com.finops.backend.entity.ResourceState;
import com.finops.backend.entity.ResourceTag;
import com.finops.backend.entity.ResourceType;
import com.finops.backend.entity.User;
import com.finops.backend.exception.ResourceNotFoundException;
import com.finops.backend.repository.AwsAccountRepository;
import com.finops.backend.repository.AwsResourceRepository;
import com.finops.backend.repository.RegionRepository;
import com.finops.backend.repository.ResourceStateRepository;
import com.finops.backend.repository.ResourceTypeRepository;
import com.finops.backend.repository.UserRepository;

@Service
public class AwsResourceService {

    private final ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    private final ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    private final ObjectProvider<RegionRepository> regionRepositoryProvider;
    private final ObjectProvider<ResourceTypeRepository> resourceTypeRepositoryProvider;
    private final ObjectProvider<ResourceStateRepository> resourceStateRepositoryProvider;
    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public AwsResourceService(
            ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider,
            ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider,
            ObjectProvider<RegionRepository> regionRepositoryProvider,
            ObjectProvider<ResourceTypeRepository> resourceTypeRepositoryProvider,
            ObjectProvider<ResourceStateRepository> resourceStateRepositoryProvider,
            ObjectProvider<UserRepository> userRepositoryProvider) {
        this.awsResourceRepositoryProvider = awsResourceRepositoryProvider;
        this.awsAccountRepositoryProvider = awsAccountRepositoryProvider;
        this.regionRepositoryProvider = regionRepositoryProvider;
        this.resourceTypeRepositoryProvider = resourceTypeRepositoryProvider;
        this.resourceStateRepositoryProvider = resourceStateRepositoryProvider;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    private User getUserByEmail(String email) {
        UserRepository repo = userRepositoryProvider.getIfAvailable();
        if (repo == null) {
            throw new IllegalStateException("Database repository is not available in current profile");
        }
        return repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public AwsResourceResponse createResource(String userEmail, CreateAwsResourceRequest request) {
        AwsAccountRepository accountRepo = awsAccountRepositoryProvider.getIfAvailable();
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        RegionRepository regionRepo = regionRepositoryProvider.getIfAvailable();
        ResourceTypeRepository typeRepo = resourceTypeRepositoryProvider.getIfAvailable();
        ResourceStateRepository stateRepo = resourceStateRepositoryProvider.getIfAvailable();

        if (accountRepo == null || resourceRepo == null || regionRepo == null || typeRepo == null || stateRepo == null) {
            throw new IllegalStateException("Repositories not available");
        }

        User user = getUserByEmail(userEmail);
        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        AwsAccount account = isAdmin
                ? accountRepo.findById(request.getAwsAccountId())
                        .orElseThrow(() -> new IllegalArgumentException("AWS Account not found"))
                : accountRepo.findByIdAndUser(request.getAwsAccountId(), user)
                        .orElseThrow(() -> new IllegalArgumentException("AWS Account not found or unauthorized"));

        Region region = regionRepo.findByRegionCode(request.getRegionCode())
                .orElseThrow(() -> new IllegalArgumentException("Region not found: " + request.getRegionCode()));

        ResourceType type = typeRepo.findByResourceTypeName(request.getResourceTypeName())
                .orElseThrow(() -> new IllegalArgumentException("Resource type not found: " + request.getResourceTypeName()));

        ResourceState state = stateRepo.findByStateNameAndResourceType(request.getStateName(), type)
                .orElseThrow(() -> new IllegalArgumentException("Resource state not found for type"));

        String arn = request.getResourceArn();
        if (arn == null || arn.isBlank()) {
            arn = String.format("arn:aws:%s:%s:%s:resource/%s",
                    type.getService().getServiceName().toLowerCase(),
                    region.getRegionCode(),
                    account.getAwsAccountNumber(),
                    request.getResourceName() != null ? request.getResourceName() : "res-" + System.currentTimeMillis());
        }

        if (resourceRepo.findByResourceArn(arn).isPresent()) {
            throw new IllegalArgumentException("Resource ARN already exists: " + arn);
        }

        AwsResource resource = new AwsResource(account, region, state, request.getResourceName(), arn);
        
        if (request.getTags() != null) {
            for (ResourceTagDto tagDto : request.getTags()) {
                resource.addTag(new ResourceTag(resource, tagDto.getTagKey(), tagDto.getTagValue()));
            }
        }

        AwsResource savedResource = resourceRepo.save(resource);
        return mapToResponse(savedResource);
    }

    private boolean isCompanyWideRole(User user) {
        if (user.getRole() == null) return false;
        String r = user.getRole().getRoleName();
        return "ADMIN".equalsIgnoreCase(r) || "USER".equalsIgnoreCase(r);
    }

    @Transactional(readOnly = true)
    public List<AwsResourceResponse> getResourcesByUser(String userEmail) {
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        if (resourceRepo == null) return List.of();

        User user = getUserByEmail(userEmail);
        List<AwsResource> resources = isCompanyWideRole(user)
                ? resourceRepo.findAll()
                : resourceRepo.findAllByUserId(user.getId());

        return resources.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AwsResourceResponse getResourceById(String userEmail, Integer resourceId) {
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        if (resourceRepo == null) throw new IllegalStateException("Repository not available");

        User user = getUserByEmail(userEmail);

        AwsResource resource = isCompanyWideRole(user)
                ? resourceRepo.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Resource not found"))
                : resourceRepo.findByIdAndUserId(resourceId, user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Resource not found or unauthorized"));

        return mapToResponse(resource);
    }

    private AwsResourceResponse mapToResponse(AwsResource resource) {
        AwsResourceResponse response = new AwsResourceResponse();
        response.setId(resource.getId());
        response.setAwsAccountNumber(resource.getAwsAccount().getAwsAccountNumber());
        response.setRegionCode(resource.getRegion().getRegionCode());
        response.setServiceName(resource.getResourceState().getResourceType().getService().getServiceName());
        response.setResourceTypeName(resource.getResourceState().getResourceType().getResourceTypeName());
        response.setStateName(resource.getResourceState().getStateName());
        response.setResourceName(resource.getResourceName());
        response.setResourceArn(resource.getResourceArn());
        response.setDiscoveredAt(resource.getDiscoveredAt());

        response.setTags(resource.getTags().stream()
                .map(t -> new ResourceTagDto(t.getTagKey(), t.getTagValue()))
                .collect(Collectors.toList()));

        return response;
    }
}

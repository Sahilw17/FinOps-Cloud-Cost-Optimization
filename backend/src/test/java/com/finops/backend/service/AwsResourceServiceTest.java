package com.finops.backend.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import com.finops.backend.dto.AwsResourceResponse;
import com.finops.backend.dto.CreateAwsResourceRequest;
import com.finops.backend.dto.ResourceTagDto;
import com.finops.backend.entity.AwsAccount;
import com.finops.backend.entity.AwsResource;
import com.finops.backend.entity.Region;
import com.finops.backend.entity.ResourceState;
import com.finops.backend.entity.ResourceType;
import com.finops.backend.entity.AwsService;
import com.finops.backend.entity.User;
import com.finops.backend.repository.AwsAccountRepository;
import com.finops.backend.repository.AwsResourceRepository;
import com.finops.backend.repository.RegionRepository;
import com.finops.backend.repository.ResourceStateRepository;
import com.finops.backend.repository.ResourceTypeRepository;
import com.finops.backend.repository.UserRepository;

class AwsResourceServiceTest {

    @Mock
    private ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    @Mock
    private ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    @Mock
    private ObjectProvider<RegionRepository> regionRepositoryProvider;
    @Mock
    private ObjectProvider<ResourceTypeRepository> resourceTypeRepositoryProvider;
    @Mock
    private ObjectProvider<ResourceStateRepository> resourceStateRepositoryProvider;
    @Mock
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Mock
    private AwsResourceRepository resourceRepo;
    @Mock
    private AwsAccountRepository accountRepo;
    @Mock
    private RegionRepository regionRepo;
    @Mock
    private ResourceTypeRepository typeRepo;
    @Mock
    private ResourceStateRepository stateRepo;
    @Mock
    private UserRepository userRepo;

    private AwsResourceService awsResourceService;

    private User testUser;
    private AwsAccount testAccount;
    private Region testRegion;
    private ResourceType testType;
    private ResourceState testState;
    private AwsService testService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(awsResourceRepositoryProvider.getIfAvailable()).thenReturn(resourceRepo);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(accountRepo);
        when(regionRepositoryProvider.getIfAvailable()).thenReturn(regionRepo);
        when(resourceTypeRepositoryProvider.getIfAvailable()).thenReturn(typeRepo);
        when(resourceStateRepositoryProvider.getIfAvailable()).thenReturn(stateRepo);
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepo);

        awsResourceService = new AwsResourceService(
                awsResourceRepositoryProvider,
                awsAccountRepositoryProvider,
                regionRepositoryProvider,
                resourceTypeRepositoryProvider,
                resourceStateRepositoryProvider,
                userRepositoryProvider
        );

        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(1);
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        testAccount = mock(AwsAccount.class);
        when(testAccount.getAwsAccountNumber()).thenReturn("123456789012");
        when(testAccount.getUser()).thenReturn(testUser);

        testRegion = new Region("us-east-1", "US East");
        
        testService = new AwsService("EC2", "AWS/EC2");

        testType = new ResourceType(testService, "ec2:instance");
        testState = new ResourceState(testType, "RUNNING");
    }

    @Test
    void testCreateResource_Success() {
        CreateAwsResourceRequest request = new CreateAwsResourceRequest();
        request.setAwsAccountId(1);
        request.setRegionCode("us-east-1");
        request.setResourceTypeName("ec2:instance");
        request.setStateName("RUNNING");
        request.setResourceName("my-instance");
        request.setResourceArn("arn:aws:ec2:...");
        request.setTags(List.of(new ResourceTagDto("env", "prod")));

        when(accountRepo.findByIdAndUser(1, testUser)).thenReturn(Optional.of(testAccount));
        when(regionRepo.findByRegionCode("us-east-1")).thenReturn(Optional.of(testRegion));
        when(typeRepo.findByResourceTypeName("ec2:instance")).thenReturn(Optional.of(testType));
        when(stateRepo.findByStateNameAndResourceType("RUNNING", testType)).thenReturn(Optional.of(testState));
        when(resourceRepo.findByResourceArn("arn:aws:ec2:...")).thenReturn(Optional.empty());
        when(resourceRepo.save(any(AwsResource.class))).thenAnswer(i -> i.getArgument(0));

        AwsResourceResponse response = awsResourceService.createResource("test@example.com", request);

        assertNotNull(response);
        assertEquals("123456789012", response.getAwsAccountNumber());
        assertEquals("my-instance", response.getResourceName());
        assertEquals(1, response.getTags().size());
        assertEquals("prod", response.getTags().get(0).getTagValue());
    }

    @Test
    void testCreateResource_UnauthorizedAccount() {
        CreateAwsResourceRequest request = new CreateAwsResourceRequest();
        request.setAwsAccountId(1);

        when(accountRepo.findByIdAndUser(1, testUser)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            awsResourceService.createResource("test@example.com", request);
        });
    }
}

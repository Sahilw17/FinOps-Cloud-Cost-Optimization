package com.finops.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finops.backend.dto.AwsResourceResponse;
import com.finops.backend.dto.CreateAwsResourceRequest;
import com.finops.backend.service.AwsResourceService;

class AwsResourceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AwsResourceService awsResourceService;

    @InjectMocks
    private AwsResourceController awsResourceController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Authentication auth;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(awsResourceController).build();
        auth = new UsernamePasswordAuthenticationToken("test@example.com", null);
    }

    @Test
    void testCreateResource_Success() throws Exception {
        CreateAwsResourceRequest request = new CreateAwsResourceRequest();
        request.setAwsAccountId(1);
        request.setRegionCode("us-east-1");
        request.setResourceTypeName("ec2:instance");
        request.setStateName("RUNNING");
        request.setResourceName("my-instance");
        request.setResourceArn("arn:aws:ec2:...");

        AwsResourceResponse mockResponse = new AwsResourceResponse();
        mockResponse.setAwsAccountNumber("123456789012");
        mockResponse.setResourceName("my-instance");

        when(awsResourceService.createResource(eq("test@example.com"), any(CreateAwsResourceRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.awsAccountNumber").value("123456789012"))
                .andExpect(jsonPath("$.resourceName").value("my-instance"));
    }
}


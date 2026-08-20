package com.finops.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

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
import com.finops.backend.dto.AwsAccountResponse;
import com.finops.backend.dto.CreateAwsAccountRequest;
import com.finops.backend.dto.UpdateAwsAccountStatusRequest;
import com.finops.backend.entity.AccountStatus;
import com.finops.backend.service.AwsAccountService;

public class AwsAccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AwsAccountService awsAccountService;

    @InjectMocks
    private AwsAccountController awsAccountController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Authentication auth;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(awsAccountController).build();
        auth = new UsernamePasswordAuthenticationToken("test@example.com", null);
    }

    @Test
    void testCreateAwsAccount() throws Exception {
        CreateAwsAccountRequest request = new CreateAwsAccountRequest("123456789012", "Dev Account", "AKIA123", "SECRET123");
        AwsAccountResponse response = new AwsAccountResponse(1, "123456789012", "Dev Account", AccountStatus.ACTIVE, "AKIA******3", LocalDateTime.now());

        when(awsAccountService.createAwsAccount(eq("test@example.com"), any(CreateAwsAccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(auth))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.awsAccountNumber").value("123456789012"))
                .andExpect(jsonPath("$.maskedAccessKey").value("AKIA******3"));
    }

    @Test
    void testGetUserAwsAccounts() throws Exception {
        AwsAccountResponse response = new AwsAccountResponse(1, "123456789012", "Dev Account", AccountStatus.ACTIVE, "AKIA******3", LocalDateTime.now());

        when(awsAccountService.getUserAwsAccounts("test@example.com")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts")
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].awsAccountNumber").value("123456789012"));
    }

    @Test
    void testGetAwsAccountById() throws Exception {
        AwsAccountResponse response = new AwsAccountResponse(1, "123456789012", "Dev Account", AccountStatus.ACTIVE, "AKIA******3", LocalDateTime.now());

        when(awsAccountService.getAwsAccountById("test@example.com", 1)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/1")
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.awsAccountNumber").value("123456789012"));
    }

    @Test
    void testUpdateAwsAccountStatus() throws Exception {
        UpdateAwsAccountStatusRequest request = new UpdateAwsAccountStatusRequest(AccountStatus.INACTIVE);
        AwsAccountResponse response = new AwsAccountResponse(1, "123456789012", "Dev Account", AccountStatus.INACTIVE, "AKIA******3", LocalDateTime.now());

        when(awsAccountService.updateAccountStatus("test@example.com", 1, AccountStatus.INACTIVE)).thenReturn(response);

        mockMvc.perform(put("/api/accounts/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void testDeleteAwsAccount() throws Exception {
        doNothing().when(awsAccountService).deleteAwsAccount("test@example.com", 1);

        mockMvc.perform(delete("/api/accounts/1")
                .principal(auth))
                .andExpect(status().isNoContent());
    }
}

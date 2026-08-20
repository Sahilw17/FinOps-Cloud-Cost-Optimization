package com.finops.backend.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finops.backend.dto.UpdateUserRoleRequest;
import com.finops.backend.dto.UserResponse;
import com.finops.backend.exception.GlobalExceptionHandler;
import com.finops.backend.service.AdminService;

@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();

        mockUserResponse = new UserResponse();
        mockUserResponse.setId(1);
        mockUserResponse.setEmail("test@example.com");
        mockUserResponse.setRole("FINOPS_ENGINEER");
        mockUserResponse.setName("Test User");
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(adminService.getAllUsers()).thenReturn(List.of(mockUserResponse));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void testGetUserById() throws Exception {
        when(adminService.getUserById(1)).thenReturn(mockUserResponse);

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testUpdateUserRole() throws Exception {
        UpdateUserRoleRequest request = new UpdateUserRoleRequest();
        request.setRoleName("ADMIN");

        UserResponse updatedResponse = new UserResponse();
        updatedResponse.setId(1);
        updatedResponse.setEmail("test@example.com");
        updatedResponse.setRole("ADMIN");
        updatedResponse.setName("Test User");

        when(adminService.updateUserRole(anyInt(), anyString())).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/admin/users/1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}

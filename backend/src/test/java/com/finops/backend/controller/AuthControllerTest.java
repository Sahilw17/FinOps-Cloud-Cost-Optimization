package com.finops.backend.controller;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.finops.backend.dto.AuthResponse;
import com.finops.backend.dto.LoginRequest;
import com.finops.backend.dto.RegisterRequest;
import com.finops.backend.dto.UserResponse;
import com.finops.backend.exception.EmailAlreadyExistsException;
import com.finops.backend.exception.GlobalExceptionHandler;
import com.finops.backend.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        UserResponse userResponse = new UserResponse(1, "Test User", "test@example.com", "FINOPS_ENGINEER", LocalDateTime.now());
        AuthResponse authResponse = new AuthResponse("mock.token.jwt", "Bearer", 86400000L, userResponse);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        String jsonPayload = """
                {
                    "name": "Test User",
                    "email": "test@example.com",
                    "password": "password123",
                    "roleName": "FINOPS_ENGINEER"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock.token.jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.role").value("FINOPS_ENGINEER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    void testRegisterValidationFailure() throws Exception {
        String invalidPayload = """
                {
                    "name": "",
                    "email": "not-an-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void testRegisterEmailConflict() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email is already registered: duplicate@example.com"));

        String jsonPayload = """
                {
                    "name": "Duplicate User",
                    "email": "duplicate@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email Conflict"))
                .andExpect(jsonPath("$.message").value("Email is already registered: duplicate@example.com"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        UserResponse userResponse = new UserResponse(1, "Test User", "test@example.com", "ADMIN", LocalDateTime.now());
        AuthResponse authResponse = new AuthResponse("mock.token.jwt", "Bearer", 86400000L, userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        String jsonPayload = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.token.jwt"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void testGetCurrentUserWithAuth() throws Exception {
        UserResponse userResponse = new UserResponse(1, "Auth User", "auth@example.com", "ADMIN", LocalDateTime.now());
        when(authService.getCurrentUser("auth@example.com")).thenReturn(userResponse);

        Authentication auth = new UsernamePasswordAuthenticationToken("auth@example.com", null);

        mockMvc.perform(get("/api/auth/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("auth@example.com"))
                .andExpect(jsonPath("$.name").value("Auth User"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testGetCurrentUserWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}

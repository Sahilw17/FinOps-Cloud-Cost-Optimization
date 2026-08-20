package com.finops.backend.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.finops.backend.dto.AuthResponse;
import com.finops.backend.dto.LoginRequest;
import com.finops.backend.dto.RegisterRequest;
import com.finops.backend.dto.UserResponse;
import com.finops.backend.entity.Role;
import com.finops.backend.entity.User;
import com.finops.backend.exception.EmailAlreadyExistsException;
import com.finops.backend.exception.ResourceNotFoundException;
import com.finops.backend.repository.RoleRepository;
import com.finops.backend.repository.UserRepository;
import com.finops.backend.security.CustomUserDetails;
import com.finops.backend.security.JwtService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Mock
    private ObjectProvider<RoleRepository> roleRepositoryProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepositoryProvider,
                roleRepositoryProvider,
                passwordEncoder,
                jwtService,
                authenticationManager
        );
    }

    @Test
    void testRegisterSuccess() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(roleRepositoryProvider.getIfAvailable()).thenReturn(roleRepository);

        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123", "ADMIN");
        Role role = new Role("ADMIN");
        User savedUser = new User(role, "Test User", "test@example.com", "hashedPassword");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(CustomUserDetails.class), any(), eq("ADMIN"))).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationTimeMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("Test User", response.getUser().getName());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("ADMIN", response.getUser().getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmailThrowsException() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(roleRepositoryProvider.getIfAvailable()).thenReturn(roleRepository);

        RegisterRequest request = new RegisterRequest("Test User", "existing@example.com", "password123", "ADMIN");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void testRegisterRoleNotFoundThrowsException() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(roleRepositoryProvider.getIfAvailable()).thenReturn(roleRepository);

        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "password123", "UNKNOWN_ROLE");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName("UNKNOWN_ROLE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.register(request));
    }

    @Test
    void testLoginSuccess() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        LoginRequest request = new LoginRequest("test@example.com", "password123");
        Role role = new Role("FINOPS_ENGINEER");
        User user = new User(role, "Test User", "test@example.com", "hashedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(CustomUserDetails.class), any(), eq("FINOPS_ENGINEER"))).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationTimeMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("test@example.com", response.getUser().getEmail());
    }

    @Test
    void testLoginWrongPasswordThrowsBadCredentials() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        Role role = new Role("FINOPS_ENGINEER");
        User user = new User(role, "Test User", "test@example.com", "hashedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void testLoginInvalidUserThrowsBadCredentials() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void testGetCurrentUser() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        Role role = new Role("ADMIN");
        User user = new User(role, "Admin User", "admin@example.com", "hashedPassword");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        UserResponse response = authService.getCurrentUser("admin@example.com");

        assertNotNull(response);
        assertEquals("admin@example.com", response.getEmail());
        assertEquals("Admin User", response.getName());
        assertEquals("ADMIN", response.getRole());
    }
}

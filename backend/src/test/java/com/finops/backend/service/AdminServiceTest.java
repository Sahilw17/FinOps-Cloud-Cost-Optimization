package com.finops.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import com.finops.backend.dto.UserResponse;
import com.finops.backend.entity.Role;
import com.finops.backend.entity.User;
import com.finops.backend.exception.ResourceNotFoundException;
import com.finops.backend.repository.RoleRepository;
import com.finops.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Mock
    private ObjectProvider<RoleRepository> roleRepositoryProvider;

    private AdminService adminService;

    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepositoryProvider, roleRepositoryProvider);
        mockRole = new Role("FINOPS_ENGINEER");
        mockUser = new User(mockRole, "Test User", "test@example.com", "hash");
    }

    @Test
    void testGetAllUsers() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        List<UserResponse> result = adminService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).getEmail());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));

        UserResponse result = adminService.getUserById(1);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getUserById(1));
    }

    @Test
    void testUpdateUserRole_Success() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(roleRepositoryProvider.getIfAvailable()).thenReturn(roleRepository);

        Role newRole = new Role("ADMIN");
        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(newRole));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse result = adminService.updateUserRole(1, "ADMIN");

        assertNotNull(result);
        assertEquals("ADMIN", result.getRole());
        verify(userRepository).save(mockUser);
    }

    @Test
    void testUpdateUserRole_UserNotFound() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(roleRepositoryProvider.getIfAvailable()).thenReturn(roleRepository);

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.updateUserRole(1, "ADMIN"));
    }

    @Test
    void testUpdateUserRole_RoleNotFound() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(roleRepositoryProvider.getIfAvailable()).thenReturn(roleRepository);

        when(userRepository.findById(1)).thenReturn(Optional.of(mockUser));
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.updateUserRole(1, "ADMIN"));
    }
}

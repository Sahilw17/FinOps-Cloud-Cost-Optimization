package com.finops.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finops.backend.dto.UserResponse;
import com.finops.backend.entity.Role;
import com.finops.backend.entity.User;
import com.finops.backend.exception.ResourceNotFoundException;
import com.finops.backend.repository.RoleRepository;
import com.finops.backend.repository.UserRepository;

@Service
public class AdminService {

    private final ObjectProvider<UserRepository> userRepositoryProvider;
    private final ObjectProvider<RoleRepository> roleRepositoryProvider;

    public AdminService(ObjectProvider<UserRepository> userRepositoryProvider, ObjectProvider<RoleRepository> roleRepositoryProvider) {
        this.userRepositoryProvider = userRepositoryProvider;
        this.roleRepositoryProvider = roleRepositoryProvider;
    }

    private UserRepository getUserRepository() {
        UserRepository repo = userRepositoryProvider.getIfAvailable();
        if (repo == null) {
            throw new IllegalStateException("Database repository is not available in current profile");
        }
        return repo;
    }

    private RoleRepository getRoleRepository() {
        RoleRepository repo = roleRepositoryProvider.getIfAvailable();
        if (repo == null) {
            throw new IllegalStateException("Database repository is not available in current profile");
        }
        return repo;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return getUserRepository().findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer id) {
        User user = getUserRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserRole(Integer id, String roleName) {
        UserRepository userRepository = getUserRepository();
        RoleRepository roleRepository = getRoleRepository();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().getRoleName());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}

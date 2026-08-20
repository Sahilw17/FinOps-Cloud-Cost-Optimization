package com.finops.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final ObjectProvider<UserRepository> userRepositoryProvider;
    private final ObjectProvider<RoleRepository> roleRepositoryProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            ObjectProvider<UserRepository> userRepositoryProvider,
            ObjectProvider<RoleRepository> roleRepositoryProvider,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepositoryProvider = userRepositoryProvider;
        this.roleRepositoryProvider = roleRepositoryProvider;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserRepository userRepository = getUserRepository();
        RoleRepository roleRepository = getRoleRepository();

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered: " + request.getEmail());
        }

        String targetRoleName = (request.getRoleName() != null && !request.getRoleName().trim().isEmpty())
                ? request.getRoleName().trim().toUpperCase()
                : "FINOPS_ENGINEER";

        Role role = roleRepository.findByRoleName(targetRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + targetRoleName));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(role, request.getName(), request.getEmail(), encodedPassword);
        User savedUser = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String token = jwtService.generateToken(userDetails, savedUser.getId(), role.getRoleName());

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationTimeMs(),
                mapToUserResponse(savedUser)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserRepository userRepository = getUserRepository();
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User with email '{}' not found", request.getEmail());
                    return new BadCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Password mismatch for email '{}'", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "FINOPS_ENGINEER";
        String token = jwtService.generateToken(userDetails, user.getId(), roleName);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationTimeMs(),
                mapToUserResponse(user)
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        UserRepository userRepository = getUserRepository();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    public UserResponse mapToUserResponse(User user) {
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : null;
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roleName,
                user.getCreatedAt()
        );
    }
}

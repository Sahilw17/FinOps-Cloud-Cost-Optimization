package com.finops.backend.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.finops.backend.entity.User;
import com.finops.backend.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public CustomUserDetailsService(ObjectProvider<UserRepository> userRepositoryProvider) {
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserRepository userRepository = userRepositoryProvider.getIfAvailable();
        if (userRepository == null) {
            throw new UsernameNotFoundException("Database repository is not active in the current profile.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new CustomUserDetails(user);
    }
}

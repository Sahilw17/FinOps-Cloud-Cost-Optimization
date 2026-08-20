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

import com.finops.backend.dto.AwsAccountResponse;
import com.finops.backend.dto.CreateAwsAccountRequest;
import com.finops.backend.entity.AccountStatus;
import com.finops.backend.entity.AwsAccount;
import com.finops.backend.entity.AwsCredentials;
import com.finops.backend.entity.Role;
import com.finops.backend.entity.User;
import com.finops.backend.exception.ResourceNotFoundException;
import com.finops.backend.repository.AwsAccountRepository;
import com.finops.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AwsAccountServiceTest {

    @Mock
    private AwsAccountRepository awsAccountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;

    @Mock
    private ObjectProvider<UserRepository> userRepositoryProvider;

    private AwsAccountService awsAccountService;

    private User mockUser;
    private AwsCredentials mockCredentials;
    private AwsAccount mockAccount;

    @BeforeEach
    void setUp() {
        awsAccountService = new AwsAccountService(awsAccountRepositoryProvider, userRepositoryProvider);
        Role role = new Role("FINOPS_ENGINEER");
        mockUser = new User(role, "Test User", "test@example.com", "hash");
        mockCredentials = new AwsCredentials("AKIA1234567890", "supersecretkey");
        mockAccount = new AwsAccount(mockUser, mockCredentials, "123456789012", "Prod Account");
    }

    @Test
    void testCreateAwsAccount_Success() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);
        
        when(awsAccountRepository.existsByAwsAccountNumber("123456789012")).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(awsAccountRepository.save(any(AwsAccount.class))).thenReturn(mockAccount);

        CreateAwsAccountRequest request = new CreateAwsAccountRequest("123456789012", "Prod Account", "AKIA1234567890", "supersecretkey");
        AwsAccountResponse response = awsAccountService.createAwsAccount("test@example.com", request);

        assertNotNull(response);
        assertEquals("Prod Account", response.getAccountName());
        assertEquals("AKIA**********90", response.getMaskedAccessKey());
    }

    @Test
    void testCreateAwsAccount_DuplicateThrowsException() {
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);
        when(awsAccountRepository.existsByAwsAccountNumber("123456789012")).thenReturn(true);

        CreateAwsAccountRequest request = new CreateAwsAccountRequest("123456789012", "Prod Account", "AKIA", "SECRET");

        assertThrows(IllegalArgumentException.class, () -> awsAccountService.createAwsAccount("test@example.com", request));
    }

    @Test
    void testGetUserAwsAccounts() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(awsAccountRepository.findByUser(mockUser)).thenReturn(List.of(mockAccount));

        List<AwsAccountResponse> list = awsAccountService.getUserAwsAccounts("test@example.com");
        assertEquals(1, list.size());
        assertEquals("123456789012", list.get(0).getAwsAccountNumber());
    }

    @Test
    void testGetAwsAccountById_Success() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(awsAccountRepository.findByIdAndUser(1, mockUser)).thenReturn(Optional.of(mockAccount));

        AwsAccountResponse response = awsAccountService.getAwsAccountById("test@example.com", 1);
        assertNotNull(response);
        assertEquals("Prod Account", response.getAccountName());
    }

    @Test
    void testUpdateAccountStatus() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(awsAccountRepository.findByIdAndUser(1, mockUser)).thenReturn(Optional.of(mockAccount));
        when(awsAccountRepository.save(any(AwsAccount.class))).thenReturn(mockAccount);

        AwsAccountResponse response = awsAccountService.updateAccountStatus("test@example.com", 1, AccountStatus.INACTIVE);
        assertNotNull(response);
    }

    @Test
    void testDeleteAwsAccount() {
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(awsAccountRepository.findByIdAndUser(1, mockUser)).thenReturn(Optional.of(mockAccount));

        awsAccountService.deleteAwsAccount("test@example.com", 1);

        verify(awsAccountRepository).delete(mockAccount);
    }
}

package com.finops.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finops.backend.dto.AwsAccountResponse;
import com.finops.backend.dto.CreateAwsAccountRequest;
import com.finops.backend.entity.AccountStatus;
import com.finops.backend.entity.AwsAccount;
import com.finops.backend.entity.AwsCredentials;
import com.finops.backend.entity.User;
import com.finops.backend.exception.ResourceNotFoundException;
import com.finops.backend.repository.AwsAccountRepository;
import com.finops.backend.repository.UserRepository;

@Service
public class AwsAccountService {

    private static final Logger log = LoggerFactory.getLogger(AwsAccountService.class);

    private final ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public AwsAccountService(
            ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider,
            ObjectProvider<UserRepository> userRepositoryProvider) {
        this.awsAccountRepositoryProvider = awsAccountRepositoryProvider;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    private AwsAccountRepository getAwsAccountRepository() {
        AwsAccountRepository repo = awsAccountRepositoryProvider.getIfAvailable();
        if (repo == null) {
            throw new IllegalStateException("Database repository is not available in current profile");
        }
        return repo;
    }

    private UserRepository getUserRepository() {
        UserRepository repo = userRepositoryProvider.getIfAvailable();
        if (repo == null) {
            throw new IllegalStateException("Database repository is not available in current profile");
        }
        return repo;
    }

    private User getUserByEmail(String email) {
        return getUserRepository().findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public AwsAccountResponse createAwsAccount(String userEmail, CreateAwsAccountRequest request) {
        AwsAccountRepository awsAccountRepository = getAwsAccountRepository();
        
        if (awsAccountRepository.existsByAwsAccountNumber(request.getAwsAccountNumber())) {
            throw new IllegalArgumentException("AWS Account Number is already registered.");
        }

        User user = getUserByEmail(userEmail);
        AwsCredentials credentials = new AwsCredentials(request.getAccessKey(), request.getSecretKey());
        
        AwsAccount account = new AwsAccount(user, credentials, request.getAwsAccountNumber(), request.getAccountName());
        
        AwsAccount savedAccount = awsAccountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    private boolean isCompanyWideRole(User user) {
        if (user.getRole() == null) return false;
        String r = user.getRole().getRoleName();
        return "ADMIN".equalsIgnoreCase(r) || "USER".equalsIgnoreCase(r);
    }

    @Transactional(readOnly = true)
    public List<AwsAccountResponse> getUserAwsAccounts(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<AwsAccount> accounts = isCompanyWideRole(user)
                ? getAwsAccountRepository().findAll()
                : getAwsAccountRepository().findByUser(user);
        return accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AwsAccountResponse getAwsAccountById(String userEmail, Integer accountId) {
        User user = getUserByEmail(userEmail);
        AwsAccount account = isCompanyWideRole(user)
                ? getAwsAccountRepository().findById(accountId)
                        .orElseThrow(() -> new ResourceNotFoundException("AWS Account not found."))
                : getAwsAccountRepository().findByIdAndUser(accountId, user)
                        .orElseThrow(() -> new ResourceNotFoundException("AWS Account not found or access denied."));
        return mapToResponse(account);
    }

    @Transactional
    public AwsAccountResponse updateAccountStatus(String userEmail, Integer accountId, AccountStatus newStatus) {
        User user = getUserByEmail(userEmail);
        AwsAccountRepository awsAccountRepository = getAwsAccountRepository();
        
        AwsAccount account = isCompanyWideRole(user)
                ? awsAccountRepository.findById(accountId)
                        .orElseThrow(() -> new ResourceNotFoundException("AWS Account not found."))
                : awsAccountRepository.findByIdAndUser(accountId, user)
                        .orElseThrow(() -> new ResourceNotFoundException("AWS Account not found or access denied."));
        
        account.setStatus(newStatus);
        AwsAccount updatedAccount = awsAccountRepository.save(account);
        return mapToResponse(updatedAccount);
    }

    @Transactional
    public void deleteAwsAccount(String userEmail, Integer accountId) {
        User user = getUserByEmail(userEmail);
        AwsAccountRepository awsAccountRepository = getAwsAccountRepository();
        
        AwsAccount account = isCompanyWideRole(user)
                ? awsAccountRepository.findById(accountId)
                        .orElseThrow(() -> new ResourceNotFoundException("AWS Account not found."))
                : awsAccountRepository.findByIdAndUser(accountId, user)
                        .orElseThrow(() -> new ResourceNotFoundException("AWS Account not found or access denied."));
        
        awsAccountRepository.delete(account);
    }

    private AwsAccountResponse mapToResponse(AwsAccount account) {
        String accessKey = account.getCredentials().getAccessKey();
        String maskedAccessKey = maskAccessKey(accessKey);
        
        return new AwsAccountResponse(
                account.getId(),
                account.getAwsAccountNumber(),
                account.getAccountName(),
                account.getStatus(),
                maskedAccessKey,
                account.getCreatedAt()
        );
    }

    private String maskAccessKey(String accessKey) {
        if (accessKey == null || accessKey.length() < 6) {
            return "******";
        }
        return accessKey.substring(0, 4) + "**********" + accessKey.substring(accessKey.length() - 2);
    }
}

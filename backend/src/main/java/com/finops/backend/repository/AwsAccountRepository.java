package com.finops.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finops.backend.entity.AwsAccount;
import com.finops.backend.entity.User;

@Repository
public interface AwsAccountRepository extends JpaRepository<AwsAccount, Integer> {

    List<AwsAccount> findByUser(User user);

    Optional<AwsAccount> findByIdAndUser(Integer id, User user);

    boolean existsByAwsAccountNumber(String awsAccountNumber);
}

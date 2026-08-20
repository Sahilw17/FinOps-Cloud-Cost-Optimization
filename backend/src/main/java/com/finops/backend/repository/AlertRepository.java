package com.finops.backend.repository;

import com.finops.backend.entity.Alert;
import com.finops.backend.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    @Query("SELECT a FROM Alert a " +
           "LEFT JOIN a.awsAccount acc1 " +
           "LEFT JOIN acc1.user u1 " +
           "LEFT JOIN a.awsResource res " +
           "LEFT JOIN res.awsAccount acc2 " +
           "LEFT JOIN acc2.user u2 " +
           "WHERE (:isAdmin = true OR u1.email = :email OR u2.email = :email) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:accountId IS NULL OR acc1.id = :accountId OR acc2.id = :accountId) " +
           "AND (:resourceId IS NULL OR res.id = :resourceId) " +
           "ORDER BY a.createdAt DESC")
    List<Alert> findAlertsForUser(
            @Param("email") String email,
            @Param("isAdmin") boolean isAdmin,
            @Param("status") AlertStatus status,
            @Param("accountId") Integer accountId,
            @Param("resourceId") Integer resourceId);

    @Query("SELECT a FROM Alert a " +
           "LEFT JOIN a.awsAccount acc1 " +
           "LEFT JOIN acc1.user u1 " +
           "LEFT JOIN a.awsResource res " +
           "LEFT JOIN res.awsAccount acc2 " +
           "LEFT JOIN acc2.user u2 " +
           "WHERE a.id = :id " +
           "AND (:isAdmin = true OR u1.email = :email OR u2.email = :email)")
    Optional<Alert> findByIdAndUserEmail(
            @Param("id") Long id,
            @Param("email") String email,
            @Param("isAdmin") boolean isAdmin);

    @Query("SELECT COUNT(a) FROM Alert a " +
           "LEFT JOIN a.awsAccount acc1 " +
           "LEFT JOIN acc1.user u1 " +
           "LEFT JOIN a.awsResource res " +
           "LEFT JOIN res.awsAccount acc2 " +
           "LEFT JOIN acc2.user u2 " +
           "WHERE (:isAdmin = true OR u1.email = :email OR u2.email = :email) " +
           "AND a.status = 'UNREAD'")
    long countUnreadAlertsForUser(
            @Param("email") String email,
            @Param("isAdmin") boolean isAdmin);
}

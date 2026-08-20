package com.finops.backend.repository;

import com.finops.backend.entity.CostHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CostHistoryRepository extends JpaRepository<CostHistory, Long> {

    @Query("SELECT c FROM CostHistory c " +
           "LEFT JOIN c.awsAccount acc1 " +
           "LEFT JOIN acc1.user u1 " +
           "LEFT JOIN c.awsResource r " +
           "LEFT JOIN r.awsAccount acc2 " +
           "LEFT JOIN acc2.user u2 " +
           "WHERE (:isAdmin = true OR u1.email = :email OR u2.email = :email) " +
           "AND (:accountId IS NULL OR acc1.id = :accountId OR acc2.id = :accountId) " +
           "AND (:resourceId IS NULL OR r.id = :resourceId) " +
           "AND c.usageDate BETWEEN :startDate AND :endDate " +
           "ORDER BY c.usageDate ASC")
    List<CostHistory> findCostsForUser(
            @Param("email") String email,
            @Param("isAdmin") boolean isAdmin,
            @Param("accountId") Integer accountId,
            @Param("resourceId") Integer resourceId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

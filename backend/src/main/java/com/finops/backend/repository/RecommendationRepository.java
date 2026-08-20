package com.finops.backend.repository;

import com.finops.backend.entity.Recommendation;
import com.finops.backend.entity.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    @Query("SELECT r FROM Recommendation r " +
           "LEFT JOIN r.awsAccount acc1 " +
           "LEFT JOIN acc1.user u1 " +
           "LEFT JOIN r.awsResource res " +
           "LEFT JOIN res.awsAccount acc2 " +
           "LEFT JOIN acc2.user u2 " +
           "WHERE (:isAdmin = true OR u1.email = :email OR u2.email = :email) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:accountId IS NULL OR acc1.id = :accountId OR acc2.id = :accountId) " +
           "AND (:resourceId IS NULL OR res.id = :resourceId) " +
           "ORDER BY r.createdAt DESC")
    List<Recommendation> findRecommendationsForUser(
            @Param("email") String email,
            @Param("isAdmin") boolean isAdmin,
            @Param("status") RecommendationStatus status,
            @Param("accountId") Integer accountId,
            @Param("resourceId") Integer resourceId);

    @Query("SELECT r FROM Recommendation r " +
           "LEFT JOIN r.awsAccount acc1 " +
           "LEFT JOIN acc1.user u1 " +
           "LEFT JOIN r.awsResource res " +
           "LEFT JOIN res.awsAccount acc2 " +
           "LEFT JOIN acc2.user u2 " +
           "WHERE r.id = :id " +
           "AND (:isAdmin = true OR u1.email = :email OR u2.email = :email)")
    Optional<Recommendation> findByIdAndUserEmail(
            @Param("id") Long id,
            @Param("email") String email,
            @Param("isAdmin") boolean isAdmin);
}

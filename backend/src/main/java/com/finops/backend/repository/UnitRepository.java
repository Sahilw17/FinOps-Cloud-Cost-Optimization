package com.finops.backend.repository;

import com.finops.backend.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {
    Optional<Unit> findByUnitName(String unitName);
}

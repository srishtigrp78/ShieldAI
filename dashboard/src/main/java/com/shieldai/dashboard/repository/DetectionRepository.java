package com.shieldai.dashboard.repository;

import com.shieldai.dashboard.entity.DetectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetectionRepository extends JpaRepository<DetectionEntity, Long> {
    // add custom queries if/when needed (paging, filters)
}


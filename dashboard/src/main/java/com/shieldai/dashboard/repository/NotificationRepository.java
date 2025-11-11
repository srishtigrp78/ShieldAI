package com.shieldai.dashboard.repository;

import com.shieldai.dashboard.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findAllByOrderByCreatedAtDesc();
    List<NotificationEntity> findByIsReadFalseOrderByCreatedAtDesc();
    long countByIsReadFalse();
}
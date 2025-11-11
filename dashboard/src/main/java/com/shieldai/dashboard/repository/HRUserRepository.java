package com.shieldai.dashboard.repository;

import com.shieldai.dashboard.entity.HRUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HRUserRepository extends JpaRepository<HRUserEntity, Long> {
    Optional<HRUserEntity> findByUsername(String username);
    Optional<HRUserEntity> findByEmail(String email);
    Optional<HRUserEntity> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
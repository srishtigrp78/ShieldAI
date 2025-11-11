package com.shieldai.dashboard.service;

import com.shieldai.dashboard.entity.HRUserEntity;
import com.shieldai.dashboard.repository.HRUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class HRUserService {
    private final HRUserRepository hrUserRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$");

    public HRUserService(HRUserRepository hrUserRepository, PasswordEncoder passwordEncoder) {
        this.hrUserRepository = hrUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public HRUserEntity signup(String username, String email, String password) {
        if (hrUserRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (hrUserRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new RuntimeException("Invalid email format");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RuntimeException("Password must be at least 8 characters with uppercase, lowercase, and number");
        }

        HRUserEntity user = new HRUserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        
        return hrUserRepository.save(user);
    }

    public Optional<HRUserEntity> authenticate(String usernameOrEmail, String password) {
        Optional<HRUserEntity> userOpt = hrUserRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        
        if (userOpt.isPresent()) {
            HRUserEntity user = userOpt.get();
            if (user.getIsActive() && passwordEncoder.matches(password, user.getPasswordHash())) {
                user.setLastLogin(LocalDateTime.now());
                hrUserRepository.save(user);
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
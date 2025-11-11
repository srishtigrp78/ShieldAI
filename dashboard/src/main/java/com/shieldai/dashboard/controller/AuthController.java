package com.shieldai.dashboard.controller;

import com.shieldai.dashboard.entity.HRUserEntity;
import com.shieldai.dashboard.security.JwtUtil;
import com.shieldai.dashboard.service.HRUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/hr")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final HRUserService hrUserService;

    public AuthController(JwtUtil jwtUtil, HRUserService hrUserService) {
        this.jwtUtil = jwtUtil;
        this.hrUserService = hrUserService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");

            HRUserEntity user = hrUserService.signup(username, email, password);
            return ResponseEntity.ok(Map.of("message", "User created successfully", "userId", user.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String usernameOrEmail = credentials.get("username");
        String password = credentials.get("password");

        Optional<HRUserEntity> userOpt = hrUserService.authenticate(usernameOrEmail, password);
        if (userOpt.isPresent()) {
            HRUserEntity user = userOpt.get();
            String token = jwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok(Map.of(
                "token", token, 
                "username", user.getUsername(),
                "email", user.getEmail(),
                "lastLogin", user.getLastLogin()
            ));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                return ResponseEntity.ok(Map.of("valid", true, "username", username));
            }
        }
        return ResponseEntity.status(401).body(Map.of("valid", false));
    }
}
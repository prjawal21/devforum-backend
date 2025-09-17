package com.devforum.backend.service;

import com.devforum.backend.dto.JwtResponse;
import com.devforum.backend.dto.LoginRequest;
import com.devforum.backend.dto.RegisterRequest;
import com.devforum.backend.entity.User;
import com.devforum.backend.repository.UserRepository;
import com.devforum.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public JwtResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .role(User.Role.USER)
                .reputation(0)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());
        
        // Generate JWT tokens
        String jwt = jwtUtil.generateTokenFromUser(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);
        
        return new JwtResponse(jwt, refreshToken, savedUser);
    }
    
    @Transactional
    public JwtResponse login(LoginRequest loginRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = (User) authentication.getPrincipal();
        
        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate JWT tokens
        String jwt = jwtUtil.generateTokenFromAuthentication(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        
        log.info("User logged in: {}", user.getUsername());
        
        return new JwtResponse(jwt, refreshToken, user);
    }
    
    public JwtResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String newJwt = jwtUtil.generateTokenFromUser(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        
        return new JwtResponse(newJwt, newRefreshToken, user);
    }
    
    public void logout(String token) {
        // In a production system, you might want to blacklist the token
        // For now, we'll just clear the security context
        SecurityContextHolder.clearContext();
        log.info("User logged out");
    }
}
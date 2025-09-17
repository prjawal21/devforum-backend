package com.devforum.backend.service;

import com.devforum.backend.dto.UserProfileDTO;
import com.devforum.backend.entity.User;
import com.devforum.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public Optional<User> getCurrentUser() {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public UserProfileDTO getCurrentUserProfile() {
        User currentUser = getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        // Include private information for own profile
        return UserProfileDTO.fromUser(currentUser, true);
    }
    
    public UserProfileDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        // Check if viewing own profile
        boolean isOwnProfile = getCurrentUser()
                .map(currentUser -> currentUser.getUsername().equals(username))
                .orElse(false);
        
        return UserProfileDTO.fromUser(user, isOwnProfile);
    }
    
    public UserProfileDTO getUserProfileById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Check if viewing own profile
        boolean isOwnProfile = getCurrentUser()
                .map(currentUser -> currentUser.getId().equals(userId))
                .orElse(false);
        
        return UserProfileDTO.fromUser(user, isOwnProfile);
    }
    
    @Transactional
    public UserProfileDTO updateProfile(String bio, String firstName, String lastName, String avatarUrl) {
        User currentUser = getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        currentUser.setBio(bio);
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setAvatarUrl(avatarUrl);
        currentUser.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(currentUser);
        log.info("User profile updated: {}", updatedUser.getUsername());
        
        return UserProfileDTO.fromUser(updatedUser, true);
    }
    
    @Transactional
    public void updateReputation(String userId, int reputationChange) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        int newReputation = Math.max(0, user.getReputation() + reputationChange);
        user.setReputation(newReputation);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.info("User reputation updated: {} -> {}", user.getUsername(), newReputation);
    }
    
    public Page<UserProfileDTO> searchUsers(String searchTerm, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(searchTerm, pageable);
        return users.map(user -> UserProfileDTO.fromUser(user, false));
    }
    
    public Page<UserProfileDTO> getTopUsersByReputation(Pageable pageable) {
        Page<User> users = userRepository.findTopUsersByReputation(pageable);
        return users.map(user -> UserProfileDTO.fromUser(user, false));
    }
    
    public Page<UserProfileDTO> getUsersByRole(User.Role role, Pageable pageable) {
        Page<User> users = userRepository.findByRole(role, pageable);
        return users.map(user -> UserProfileDTO.fromUser(user, false));
    }
    
    @Transactional
    public void changeUserRole(String userId, User.Role newRole) {
        // Only admins should be able to call this
        User currentUser = getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied: Admin role required");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.info("User role changed: {} -> {}", user.getUsername(), newRole);
    }
    
    public long getUserCount() {
        return userRepository.count();
    }
    
    public long getUserCountByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
}
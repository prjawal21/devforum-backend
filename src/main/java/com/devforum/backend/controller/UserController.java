package com.devforum.backend.controller;

import com.devforum.backend.dto.UserProfileDTO;
import com.devforum.backend.entity.User;
import com.devforum.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile() {
        try {
            UserProfileDTO profile = userService.getCurrentUserProfile();
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDTO> updateProfile(@RequestBody Map<String, String> updates) {
        try {
            String bio = updates.get("bio");
            String firstName = updates.get("firstName");
            String lastName = updates.get("lastName");
            String avatarUrl = updates.get("avatarUrl");
            
            UserProfileDTO updatedProfile = userService.updateProfile(bio, firstName, lastName, avatarUrl);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{username}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username) {
        try {
            UserProfileDTO profile = userService.getUserProfile(username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<UserProfileDTO>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("reputation").descending());
            Page<UserProfileDTO> users = userService.searchUsers(query, pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/top")
    public ResponseEntity<Page<UserProfileDTO>> getTopUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserProfileDTO> users = userService.getTopUsersByReputation(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Page<UserProfileDTO>> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<UserProfileDTO> users = userService.getUsersByRole(userRole, pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        try {
            String roleString = request.get("role");
            User.Role newRole = User.Role.valueOf(roleString.toUpperCase());
            userService.changeUserRole(userId, newRole);
            return ResponseEntity.ok(Map.of("message", "User role updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = Map.of(
                "totalUsers", userService.getUserCount(),
                "adminCount", userService.getUserCountByRole(User.Role.ADMIN),
                "moderatorCount", userService.getUserCountByRole(User.Role.MODERATOR),
                "userCount", userService.getUserCountByRole(User.Role.USER)
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
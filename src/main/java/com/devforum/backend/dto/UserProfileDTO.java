package com.devforum.backend.dto;

import com.devforum.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    
    private String id;
    private String username;
    private String email; // Only show to profile owner
    private String firstName;
    private String lastName;
    private String bio;
    private String avatarUrl;
    private Integer reputation;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    public static UserProfileDTO fromUser(User user, boolean includePrivate) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(includePrivate ? user.getEmail() : null)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .reputation(user.getReputation())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(includePrivate ? user.getLastLoginAt() : null)
                .build();
    }
}
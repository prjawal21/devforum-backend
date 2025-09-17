package com.devforum.backend.dto;

import com.devforum.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String refreshToken;
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private User.Role role;
    private Integer reputation;
    
    public JwtResponse(String accessToken, String refreshToken, User user) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
        this.reputation = user.getReputation();
    }
}
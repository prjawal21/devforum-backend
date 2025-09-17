package com.devforum.backend.controller;

import com.devforum.backend.dto.PostDTO;
import com.devforum.backend.dto.UserProfileDTO;
import com.devforum.backend.entity.User;
import com.devforum.backend.service.PostService;
import com.devforum.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class AdminController {
    
    private final UserService userService;
    private final PostService postService;
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // User statistics
            dashboard.put("totalUsers", userService.getUserCount());
            dashboard.put("adminCount", userService.getUserCountByRole(User.Role.ADMIN));
            dashboard.put("moderatorCount", userService.getUserCountByRole(User.Role.MODERATOR));
            dashboard.put("regularUserCount", userService.getUserCountByRole(User.Role.USER));
            
            // Recent activity
            Pageable recentPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
            Page<PostDTO> recentPosts = postService.getPosts(recentPageable, "recent");
            dashboard.put("recentPosts", recentPosts.getContent());
            
            // Top users
            Page<UserProfileDTO> topUsers = userService.getTopUsersByReputation(PageRequest.of(0, 5));
            dashboard.put("topUsers", topUsers.getContent());
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "USER") String role) {
        try {
            User.Role userRole = User.Role.valueOf(role.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<UserProfileDTO> users = userService.getUsersByRole(userRole, pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/users/{userId}/role")
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
    
    @GetMapping("/posts/flagged")
    public ResponseEntity<Page<PostDTO>> getFlaggedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // This would typically show posts that have been reported or have negative scores
            // For now, let's show posts sorted by score ascending (potentially problematic posts)
            Pageable pageable = PageRequest.of(page, size, Sort.by("upvotes").ascending());
            Page<PostDTO> posts = postService.getPosts(pageable, "recent");
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/posts/{postId}/moderate")
    public ResponseEntity<Map<String, String>> moderatePost(
            @PathVariable String postId,
            @RequestBody Map<String, String> action) {
        try {
            String actionType = action.get("action");
            String message = "";
            
            switch (actionType.toLowerCase()) {
                case "pin":
                    postService.pinPost(postId);
                    message = "Post pinned successfully";
                    break;
                case "lock":
                    postService.lockPost(postId);
                    message = "Post locked successfully";
                    break;
                case "delete":
                    postService.deletePost(postId);
                    message = "Post deleted successfully";
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid action"));
            }
            
            return ResponseEntity.ok(Map.of("message", message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // User stats
            stats.put("users", Map.of(
                "total", userService.getUserCount(),
                "admins", userService.getUserCountByRole(User.Role.ADMIN),
                "moderators", userService.getUserCountByRole(User.Role.MODERATOR),
                "regularUsers", userService.getUserCountByRole(User.Role.USER)
            ));
            
            // Activity stats (simplified)
            stats.put("activity", Map.of(
                "postsToday", 0, // Would need additional queries
                "commentsToday", 0,
                "newUsersToday", 0
            ));
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
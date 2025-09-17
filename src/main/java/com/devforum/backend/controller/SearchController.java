package com.devforum.backend.controller;

import com.devforum.backend.dto.PostDTO;
import com.devforum.backend.dto.UserProfileDTO;
import com.devforum.backend.service.PostService;
import com.devforum.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final PostService postService;
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> globalSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "relevance") String sort) {
        try {
            Map<String, Object> results = new HashMap<>();
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            switch (type.toLowerCase()) {
                case "posts":
                    Page<PostDTO> posts = postService.searchPosts(q, pageable);
                    results.put("posts", posts);
                    break;
                case "users":
                    Page<UserProfileDTO> users = userService.searchUsers(q, pageable);
                    results.put("users", users);
                    break;
                case "all":
                default:
                    Page<PostDTO> allPosts = postService.searchPosts(q, PageRequest.of(0, 10));
                    Page<UserProfileDTO> allUsers = userService.searchUsers(q, PageRequest.of(0, 5));
                    results.put("posts", allPosts);
                    results.put("users", allUsers);
                    break;
            }
            
            results.put("query", q);
            results.put("type", type);
            results.put("totalResults", getTotalResults(results));
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/posts")
    public ResponseEntity<Page<PostDTO>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "relevance") String sort) {
        try {
            Sort sortCriteria;
            switch (sort.toLowerCase()) {
                case "newest":
                    sortCriteria = Sort.by("createdAt").descending();
                    break;
                case "oldest":
                    sortCriteria = Sort.by("createdAt").ascending();
                    break;
                case "score":
                    sortCriteria = Sort.by("upvotes").descending();
                    break;
                case "relevance":
                default:
                    sortCriteria = Sort.by("createdAt").descending(); // MongoDB text search score would be better
                    break;
            }
            
            Pageable pageable = PageRequest.of(page, size, sortCriteria);
            Page<PostDTO> posts = postService.searchPosts(q, pageable);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/users")
    public ResponseEntity<Page<UserProfileDTO>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("reputation").descending());
            Page<UserProfileDTO> users = userService.searchUsers(q, pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(@RequestParam String q) {
        try {
            // Simple implementation - in production, you might want to use MongoDB's text search suggestions
            Map<String, Object> suggestions = new HashMap<>();
            
            // Get top matching posts (limited results for suggestions)
            Page<PostDTO> postSuggestions = postService.searchPosts(q, PageRequest.of(0, 5));
            Page<UserProfileDTO> userSuggestions = userService.searchUsers(q, PageRequest.of(0, 3));
            
            suggestions.put("posts", postSuggestions.getContent());
            suggestions.put("users", userSuggestions.getContent());
            suggestions.put("query", q);
            
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private long getTotalResults(Map<String, Object> results) {
        long total = 0;
        if (results.containsKey("posts")) {
            Page<?> posts = (Page<?>) results.get("posts");
            total += posts.getTotalElements();
        }
        if (results.containsKey("users")) {
            Page<?> users = (Page<?>) results.get("users");
            total += users.getTotalElements();
        }
        return total;
    }
}
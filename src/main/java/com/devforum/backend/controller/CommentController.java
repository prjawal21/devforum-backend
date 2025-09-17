package com.devforum.backend.controller;

import com.devforum.backend.dto.CommentDTO;
import com.devforum.backend.dto.CreateCommentRequest;
import com.devforum.backend.dto.UpdateCommentRequest;
import com.devforum.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CreateCommentRequest request) {
        try {
            CommentDTO comment = commentService.createComment(request);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsForPost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "5") int maxDepth) {
        try {
            List<CommentDTO> comments = commentService.getCommentsForPost(postId, maxDepth);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/post/{postId}/top-level")
    public ResponseEntity<Page<CommentDTO>> getTopLevelComments(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "score") String sort) {
        try {
            Sort.Direction direction = Sort.Direction.DESC;
            String sortBy = "createdAt";
            
            switch (sort.toLowerCase()) {
                case "score":
                    sortBy = "upvotes";
                    break;
                case "newest":
                    sortBy = "createdAt";
                    direction = Sort.Direction.DESC;
                    break;
                case "oldest":
                    sortBy = "createdAt";
                    direction = Sort.Direction.ASC;
                    break;
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<CommentDTO> comments = commentService.getTopLevelComments(postId, pageable);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getComment(@PathVariable String id) {
        try {
            CommentDTO comment = commentService.getComment(id);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable String id,
            @Valid @RequestBody UpdateCommentRequest request) {
        try {
            CommentDTO updatedComment = commentService.updateComment(id, request);
            return ResponseEntity.ok(updatedComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable String id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<CommentDTO>> getCommentsByUser(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<CommentDTO> comments = commentService.getCommentsByUser(username, pageable);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
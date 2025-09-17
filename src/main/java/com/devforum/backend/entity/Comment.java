package com.devforum.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "comments")
public class Comment {
    
    @Id
    private String id;
    
    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
    private String content;
    
    @DBRef
    private User author;
    
    @DBRef
    @Indexed
    private Post post;
    
    // For nested comments - reference to parent comment
    @DBRef
    private Comment parentComment;
    
    @Builder.Default
    private Integer upvotes = 0;
    
    @Builder.Default
    private Integer downvotes = 0;
    
    @Builder.Default
    private Integer level = 0; // 0 = top-level, 1 = reply to top-level, etc.
    
    @Builder.Default
    private Boolean deleted = false;
    
    @Builder.Default
    private Boolean edited = false;
    
    @Indexed
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    // For building comment tree
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();
    
    // Computed fields
    public Integer getScore() {
        return upvotes - downvotes;
    }
    
    public String getTreePath() {
        if (parentComment == null) {
            return id;
        }
        return parentComment.getTreePath() + "." + id;
    }
}
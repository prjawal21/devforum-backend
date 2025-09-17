package com.devforum.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "posts")
public class Post {
    
    @Id
    private String id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    @TextIndexed(weight = 3) // Higher weight for title in text search
    private String title;
    
    @NotBlank(message = "Body is required")
    @Size(min = 10, max = 50000, message = "Body must be between 10 and 50000 characters")
    @TextIndexed(weight = 2) // Medium weight for body in text search
    private String body;
    
    @DBRef
    private User author;
    
    @Builder.Default
    private List<String> tags = new ArrayList<>();
    
    @Builder.Default
    private Integer upvotes = 0;
    
    @Builder.Default
    private Integer downvotes = 0;
    
    @Builder.Default
    private Integer commentCount = 0;
    
    @Builder.Default
    private Integer viewCount = 0;
    
    @Builder.Default
    private Boolean pinned = false;
    
    @Builder.Default
    private Boolean locked = false;
    
    @Builder.Default
    private Boolean deleted = false;
    
    @Indexed
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivityAt;
    
    // Computed fields
    public Integer getScore() {
        return upvotes - downvotes;
    }
    
    public Double getHotScore() {
        // Basic hot score algorithm (can be improved)
        double score = getScore();
        long ageInHours = java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
        return score / Math.pow(ageInHours + 2, 1.8);
    }
}
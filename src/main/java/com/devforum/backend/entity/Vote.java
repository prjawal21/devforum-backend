package com.devforum.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "votes")
@CompoundIndex(def = "{'user': 1, 'targetType': 1, 'targetId': 1}", unique = true)
public class Vote {
    
    @Id
    private String id;
    
    @DBRef
    @Indexed
    private User user;
    
    @Indexed
    private String targetId; // ID of post or comment
    
    @Indexed
    private TargetType targetType; // POST or COMMENT
    
    private VoteType voteType; // UPVOTE or DOWNVOTE
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    public enum TargetType {
        POST, COMMENT
    }
    
    public enum VoteType {
        UPVOTE, DOWNVOTE
    }
}
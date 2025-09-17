package com.devforum.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VoteRequest {
    
    @NotBlank(message = "Vote type is required")
    @Pattern(regexp = "UPVOTE|DOWNVOTE", message = "Vote type must be UPVOTE or DOWNVOTE")
    private String voteType;
    
    @NotBlank(message = "Target type is required")
    @Pattern(regexp = "POST|COMMENT", message = "Target type must be POST or COMMENT")
    private String targetType;
    
    @NotBlank(message = "Target ID is required")
    private String targetId;
}
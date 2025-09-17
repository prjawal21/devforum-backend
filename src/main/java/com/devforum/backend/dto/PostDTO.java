package com.devforum.backend.dto;

import com.devforum.backend.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    
    private String id;
    private String title;
    private String body;
    private UserProfileDTO author;
    private List<String> tags;
    private Integer upvotes;
    private Integer downvotes;
    private Integer score;
    private Integer commentCount;
    private Integer viewCount;
    private Boolean pinned;
    private Boolean locked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivityAt;
    private Double hotScore;
    
    // User's vote on this post (if authenticated)
    private String userVote; // "UPVOTE", "DOWNVOTE", or null
    
    public static PostDTO fromPost(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .author(UserProfileDTO.fromUser(post.getAuthor(), false))
                .tags(post.getTags())
                .upvotes(post.getUpvotes())
                .downvotes(post.getDownvotes())
                .score(post.getScore())
                .commentCount(post.getCommentCount())
                .viewCount(post.getViewCount())
                .pinned(post.getPinned())
                .locked(post.getLocked())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .lastActivityAt(post.getLastActivityAt())
                .hotScore(post.getHotScore())
                .build();
    }
    
    public static PostDTO fromPostWithUserVote(Post post, String userVote) {
        PostDTO dto = fromPost(post);
        dto.setUserVote(userVote);
        return dto;
    }
}
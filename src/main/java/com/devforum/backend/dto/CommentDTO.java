package com.devforum.backend.dto;

import com.devforum.backend.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    
    private String id;
    private String content;
    private UserProfileDTO author;
    private String postId;
    private String parentCommentId;
    private Integer upvotes;
    private Integer downvotes;
    private Integer score;
    private Integer level;
    private Boolean deleted;
    private Boolean edited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User's vote on this comment (if authenticated)
    private String userVote; // "UPVOTE", "DOWNVOTE", or null
    
    // Nested replies
    @Builder.Default
    private List<CommentDTO> replies = new ArrayList<>();
    
    public static CommentDTO fromComment(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(UserProfileDTO.fromUser(comment.getAuthor(), false))
                .postId(comment.getPost().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .upvotes(comment.getUpvotes())
                .downvotes(comment.getDownvotes())
                .score(comment.getScore())
                .level(comment.getLevel())
                .deleted(comment.getDeleted())
                .edited(comment.getEdited())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
    
    public static CommentDTO fromCommentWithUserVote(Comment comment, String userVote) {
        CommentDTO dto = fromComment(comment);
        dto.setUserVote(userVote);
        return dto;
    }
    
    public static CommentDTO fromCommentWithReplies(Comment comment, String userVote, List<CommentDTO> replies) {
        CommentDTO dto = fromCommentWithUserVote(comment, userVote);
        dto.setReplies(replies);
        return dto;
    }
}
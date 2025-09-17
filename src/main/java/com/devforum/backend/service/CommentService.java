package com.devforum.backend.service;

import com.devforum.backend.dto.CommentDTO;
import com.devforum.backend.dto.CreateCommentRequest;
import com.devforum.backend.dto.UpdateCommentRequest;
import com.devforum.backend.entity.Comment;
import com.devforum.backend.entity.Post;
import com.devforum.backend.entity.User;
import com.devforum.backend.entity.Vote;
import com.devforum.backend.repository.CommentRepository;
import com.devforum.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final VoteService voteService;
    
    @Transactional
    public CommentDTO createComment(CreateCommentRequest request) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        Post post = postRepository.findByIdAndDeletedFalse(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found: " + request.getPostId()));
        
        if (post.getLocked()) {
            throw new RuntimeException("Cannot comment on locked post");
        }
        
        Comment parentComment = null;
        int level = 0;
        
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found: " + request.getParentCommentId()));
            level = parentComment.getLevel() + 1;
            
            if (level > 10) { // Limit nesting depth
                throw new RuntimeException("Maximum comment nesting depth exceeded");
            }
        }
        
        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(currentUser)
                .post(post)
                .parentComment(parentComment)
                .upvotes(0)
                .downvotes(0)
                .level(level)
                .deleted(false)
                .edited(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        
        // Update post comment count and last activity
        post.setCommentCount(post.getCommentCount() + 1);
        post.setLastActivityAt(LocalDateTime.now());
        postRepository.save(post);
        
        log.info("Comment created on post {} by {}", post.getTitle(), currentUser.getUsername());
        
        return CommentDTO.fromComment(savedComment);
    }
    
    public List<CommentDTO> getCommentsForPost(String postId, int maxDepth) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        // Get all comments for the post
        List<Comment> allComments = commentRepository.findByPostAndDeletedFalseOrderByCreatedAt(post);
        
        // Build comment tree
        return buildCommentTree(allComments, null, maxDepth, 0);
    }
    
    public Page<CommentDTO> getTopLevelComments(String postId, Pageable pageable) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        Page<Comment> comments = commentRepository.findTopLevelCommentsByPost(post, pageable);
        
        return comments.map(comment -> {
            String userVote = voteService.getUserVote(comment.getId(), Vote.TargetType.COMMENT);
            
            // Get immediate replies
            List<Comment> replies = commentRepository.findRepliesByParentComment(comment);
            List<CommentDTO> replyDTOs = replies.stream()
                    .map(reply -> {
                        String replyUserVote = voteService.getUserVote(reply.getId(), Vote.TargetType.COMMENT);
                        return CommentDTO.fromCommentWithUserVote(reply, replyUserVote);
                    })
                    .collect(Collectors.toList());
            
            return CommentDTO.fromCommentWithReplies(comment, userVote, replyDTOs);
        });
    }
    
    public CommentDTO getComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
        
        String userVote = voteService.getUserVote(commentId, Vote.TargetType.COMMENT);
        
        // Get replies
        List<Comment> replies = commentRepository.findRepliesByParentComment(comment);
        List<CommentDTO> replyDTOs = replies.stream()
                .map(reply -> {
                    String replyUserVote = voteService.getUserVote(reply.getId(), Vote.TargetType.COMMENT);
                    return CommentDTO.fromCommentWithUserVote(reply, replyUserVote);
                })
                .collect(Collectors.toList());
        
        return CommentDTO.fromCommentWithReplies(comment, userVote, replyDTOs);
    }
    
    @Transactional
    public CommentDTO updateComment(String commentId, UpdateCommentRequest request) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
        
        // Check if user is the author or has permission
        if (!comment.getAuthor().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.MODERATOR && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied: You can only edit your own comments");
        }
        
        comment.setContent(request.getContent());
        comment.setEdited(true);
        comment.setUpdatedAt(LocalDateTime.now());
        
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment updated by {}", currentUser.getUsername());
        
        String userVote = voteService.getUserVote(commentId, Vote.TargetType.COMMENT);
        return CommentDTO.fromCommentWithUserVote(updatedComment, userVote);
    }
    
    @Transactional
    public void deleteComment(String commentId) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found: " + commentId));
        
        // Check if user is the author or has permission
        if (!comment.getAuthor().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.MODERATOR && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied: You can only delete your own comments");
        }
        
        comment.setDeleted(true);
        comment.setContent("[deleted]");
        comment.setUpdatedAt(LocalDateTime.now());
        
        commentRepository.save(comment);
        
        // Update post comment count
        Post post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);
        
        log.info("Comment deleted by {}", currentUser.getUsername());
    }
    
    public Page<CommentDTO> getCommentsByUser(String username, Pageable pageable) {
        // Implementation would require getting user first, then their comments
        // Simplified version for now
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Page<Comment> comments = commentRepository.findByAuthorAndDeletedFalse(user, pageable);
        
        return comments.map(comment -> {
            String userVote = voteService.getUserVote(comment.getId(), Vote.TargetType.COMMENT);
            return CommentDTO.fromCommentWithUserVote(comment, userVote);
        });
    }
    
    private List<CommentDTO> buildCommentTree(List<Comment> allComments, String parentId, int maxDepth, int currentDepth) {
        if (currentDepth >= maxDepth) {
            return List.of();
        }
        
        // Group comments by parent ID for efficient lookup
        Map<String, List<Comment>> commentsByParent = new HashMap<>();
        for (Comment comment : allComments) {
            String key = comment.getParentComment() != null ? comment.getParentComment().getId() : null;
            commentsByParent.computeIfAbsent(key, k -> List.of()).add(comment);
        }
        
        List<Comment> topLevelComments = commentsByParent.getOrDefault(parentId, List.of());
        
        return topLevelComments.stream()
                .map(comment -> {
                    String userVote = voteService.getUserVote(comment.getId(), Vote.TargetType.COMMENT);
                    List<CommentDTO> replies = buildCommentTree(allComments, comment.getId(), maxDepth, currentDepth + 1);
                    return CommentDTO.fromCommentWithReplies(comment, userVote, replies);
                })
                .collect(Collectors.toList());
    }
}
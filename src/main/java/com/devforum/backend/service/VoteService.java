package com.devforum.backend.service;

import com.devforum.backend.entity.*;
import com.devforum.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {
    
    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    
    @Transactional
    public String vote(String targetId, Vote.TargetType targetType, Vote.VoteType voteType) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        // Check if user has already voted
        Optional<Vote> existingVote = voteRepository.findByUserAndTargetTypeAndTargetId(
            currentUser, targetType, targetId);
        
        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Same vote - remove it (toggle off)
                voteRepository.delete(vote);
                updateTargetVoteCounts(targetId, targetType);
                updateUserReputation(targetId, targetType, voteType, false);
                log.info("Vote removed: {} {} on {}", currentUser.getUsername(), voteType, targetId);
                return "removed";
            } else {
                // Different vote - update it
                Vote.VoteType oldVoteType = vote.getVoteType();
                vote.setVoteType(voteType);
                vote.setUpdatedAt(LocalDateTime.now());
                voteRepository.save(vote);
                updateTargetVoteCounts(targetId, targetType);
                
                // Update reputation (remove old vote effect, add new vote effect)
                updateUserReputation(targetId, targetType, oldVoteType, false);
                updateUserReputation(targetId, targetType, voteType, true);
                
                log.info("Vote changed: {} {} -> {} on {}", currentUser.getUsername(), 
                    oldVoteType, voteType, targetId);
                return "updated";
            }
        } else {
            // New vote
            Vote vote = Vote.builder()
                    .user(currentUser)
                    .targetId(targetId)
                    .targetType(targetType)
                    .voteType(voteType)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            voteRepository.save(vote);
            updateTargetVoteCounts(targetId, targetType);
            updateUserReputation(targetId, targetType, voteType, true);
            
            log.info("New vote: {} {} on {}", currentUser.getUsername(), voteType, targetId);
            return "created";
        }
    }
    
    private void updateTargetVoteCounts(String targetId, Vote.TargetType targetType) {
        long upvotes = voteRepository.countUpvotes(targetType, targetId);
        long downvotes = voteRepository.countDownvotes(targetType, targetId);
        
        if (targetType == Vote.TargetType.POST) {
            postRepository.findById(targetId).ifPresent(post -> {
                post.setUpvotes((int) upvotes);
                post.setDownvotes((int) downvotes);
                post.setLastActivityAt(LocalDateTime.now());
                postRepository.save(post);
            });
        } else if (targetType == Vote.TargetType.COMMENT) {
            commentRepository.findById(targetId).ifPresent(comment -> {
                comment.setUpvotes((int) upvotes);
                comment.setDownvotes((int) downvotes);
                commentRepository.save(comment);
                
                // Update post activity time when comment is voted on
                if (comment.getPost() != null) {
                    comment.getPost().setLastActivityAt(LocalDateTime.now());
                    postRepository.save(comment.getPost());
                }
            });
        }
    }
    
    private void updateUserReputation(String targetId, Vote.TargetType targetType, 
                                     Vote.VoteType voteType, boolean isAdding) {
        String authorId = null;
        int reputationChange = 0;
        
        if (targetType == Vote.TargetType.POST) {
            Optional<Post> post = postRepository.findById(targetId);
            if (post.isPresent()) {
                authorId = post.get().getAuthor().getId();
                reputationChange = voteType == Vote.VoteType.UPVOTE ? 10 : -2; // Posts give more reputation
            }
        } else if (targetType == Vote.TargetType.COMMENT) {
            Optional<Comment> comment = commentRepository.findById(targetId);
            if (comment.isPresent()) {
                authorId = comment.get().getAuthor().getId();
                reputationChange = voteType == Vote.VoteType.UPVOTE ? 5 : -1; // Comments give less reputation
            }
        }
        
        if (authorId != null) {
            if (!isAdding) {
                reputationChange = -reputationChange; // Reverse the change when removing vote
            }
            userService.updateReputation(authorId, reputationChange);
        }
    }
    
    public String getUserVote(String targetId, Vote.TargetType targetType) {
        User currentUser = userService.getCurrentUser().orElse(null);
        if (currentUser == null) {
            return null;
        }
        
        Optional<Vote> vote = voteRepository.findByUserAndTargetTypeAndTargetId(
            currentUser, targetType, targetId);
        
        return vote.map(v -> v.getVoteType().name()).orElse(null);
    }
}
package com.devforum.backend.repository;

import com.devforum.backend.entity.Vote;
import com.devforum.backend.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
    
    // Find vote by user and target
    Optional<Vote> findByUserAndTargetTypeAndTargetId(User user, Vote.TargetType targetType, String targetId);
    
    // Check if user has voted on target
    boolean existsByUserAndTargetTypeAndTargetId(User user, Vote.TargetType targetType, String targetId);
    
    // Get all votes for a target
    List<Vote> findByTargetTypeAndTargetId(Vote.TargetType targetType, String targetId);
    
    // Count votes by type for a target
    long countByTargetTypeAndTargetIdAndVoteType(Vote.TargetType targetType, String targetId, Vote.VoteType voteType);
    
    // Get user's votes
    List<Vote> findByUser(User user);
    
    // Get user's votes by target type
    List<Vote> findByUserAndTargetType(User user, Vote.TargetType targetType);
    
    // Get user's votes by vote type
    List<Vote> findByUserAndVoteType(User user, Vote.VoteType voteType);
    
    // Count upvotes for target
    @Query("{ 'targetType': ?0, 'targetId': ?1, 'voteType': 'UPVOTE' }")
    long countUpvotes(Vote.TargetType targetType, String targetId);
    
    // Count downvotes for target
    @Query("{ 'targetType': ?0, 'targetId': ?1, 'voteType': 'DOWNVOTE' }")
    long countDownvotes(Vote.TargetType targetType, String targetId);
    
    // Get vote counts for multiple targets
    @Query("{ 'targetType': ?0, 'targetId': { $in: ?1 } }")
    List<Vote> findByTargetTypeAndTargetIdIn(Vote.TargetType targetType, List<String> targetIds);
    
    // Delete all votes for a target (when content is deleted)
    void deleteByTargetTypeAndTargetId(Vote.TargetType targetType, String targetId);
    
    // Delete all votes by user
    void deleteByUser(User user);
}
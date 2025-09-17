package com.devforum.backend.repository;

import com.devforum.backend.entity.Comment;
import com.devforum.backend.entity.Post;
import com.devforum.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    
    // Find comments by post
    @Query(value = "{ 'post': ?0, 'deleted': false }", sort = "{ 'createdAt': 1 }")
    List<Comment> findByPostAndDeletedFalseOrderByCreatedAt(Post post);
    
    // Find top-level comments (no parent)
    @Query(value = "{ 'post': ?0, 'parentComment': null, 'deleted': false }", 
           sort = "{ 'upvotes': -1, 'createdAt': 1 }")
    Page<Comment> findTopLevelCommentsByPost(Post post, Pageable pageable);
    
    // Find replies to a specific comment
    @Query(value = "{ 'parentComment': ?0, 'deleted': false }", 
           sort = "{ 'upvotes': -1, 'createdAt': 1 }")
    List<Comment> findRepliesByParentComment(Comment parentComment);
    
    // Find comments by author
    @Query(value = "{ 'author': ?0, 'deleted': false }", sort = "{ 'createdAt': -1 }")
    Page<Comment> findByAuthorAndDeletedFalse(User author, Pageable pageable);
    
    // Find comments by level (depth)
    @Query("{ 'post': ?0, 'level': ?1, 'deleted': false }")
    List<Comment> findByPostAndLevel(Post post, Integer level);
    
    // Find recent comments
    @Query("{ 'createdAt': { $gte: ?0 }, 'deleted': false }")
    Page<Comment> findRecentComments(LocalDateTime since, Pageable pageable);
    
    // Find highly voted comments
    @Query(value = "{ 'upvotes': { $gte: ?0 }, 'deleted': false }", 
           sort = "{ 'upvotes': -1 }")
    Page<Comment> findHighlyVotedComments(Integer minUpvotes, Pageable pageable);
    
    // Count comments by post
    long countByPostAndDeletedFalse(Post post);
    
    // Count comments by author
    long countByAuthorAndDeletedFalse(User author);
    
    // Find comments that need moderation
    @Query("{ 'deleted': false, 'upvotes': { $lt: -3 } }")
    Page<Comment> findCommentsNeedingModeration(Pageable pageable);
    
    // Find all descendants of a comment (for deleting comment trees)
    @Query("{ 'parentComment': ?0 }")
    List<Comment> findAllDescendants(Comment parentComment);
    
    // Search comments by content
    @Query("{ 'content': { $regex: ?0, $options: 'i' }, 'deleted': false }")
    Page<Comment> searchComments(String searchTerm, Pageable pageable);
    
    // Find comments by post with pagination, sorted by score
    @Query(value = "{ 'post': ?0, 'deleted': false }")
    Page<Comment> findByPostAndDeletedFalse(Post post, Pageable pageable);
    
    // Get comment tree for a post (top-level comments with their immediate replies)
    @Query(value = "{ 'post': ?0, 'deleted': false, $or: [ { 'parentComment': null }, { 'level': { $lte: ?1 } } ] }", 
           sort = "{ 'level': 1, 'upvotes': -1, 'createdAt': 1 }")
    List<Comment> findCommentTree(Post post, Integer maxDepth);
}
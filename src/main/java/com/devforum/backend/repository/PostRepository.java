package com.devforum.backend.repository;

import com.devforum.backend.entity.Post;
import com.devforum.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    
    // Find posts by author
    Page<Post> findByAuthorAndDeletedFalse(User author, Pageable pageable);
    
    // Find posts by tag
    @Query("{ 'tags': { $in: [?0] }, 'deleted': false }")
    Page<Post> findByTagAndDeletedFalse(String tag, Pageable pageable);
    
    // Find posts by multiple tags
    @Query("{ 'tags': { $all: ?0 }, 'deleted': false }")
    Page<Post> findByAllTagsAndDeletedFalse(List<String> tags, Pageable pageable);
    
    // Find posts by any of the tags
    @Query("{ 'tags': { $in: ?0 }, 'deleted': false }")
    Page<Post> findByAnyTagAndDeletedFalse(List<String> tags, Pageable pageable);
    
    // Full-text search
    @Query("{ $text: { $search: ?0 }, 'deleted': false }")
    Page<Post> findByTextSearch(String searchText, Pageable pageable);
    
    // Find recent posts
    @Query("{ 'createdAt': { $gte: ?0 }, 'deleted': false }")
    Page<Post> findRecentPosts(LocalDateTime since, Pageable pageable);
    
    // Find trending posts (high activity recently)
    @Query("{ 'lastActivityAt': { $gte: ?0 }, 'deleted': false }")
    Page<Post> findTrendingPosts(LocalDateTime since, Pageable pageable);
    
    // Find hot posts (high score and recent)
    @Query("{ 'createdAt': { $gte: ?0 }, 'upvotes': { $gte: ?1 }, 'deleted': false }")
    Page<Post> findHotPosts(LocalDateTime since, Integer minUpvotes, Pageable pageable);
    
    // Find top posts by score
    @Query(value = "{ 'deleted': false }", sort = "{ 'upvotes': -1, 'downvotes': 1 }")
    Page<Post> findTopPosts(Pageable pageable);
    
    // Find pinned posts
    @Query(value = "{ 'pinned': true, 'deleted': false }", sort = "{ 'createdAt': -1 }")
    List<Post> findPinnedPosts();
    
    // Find posts without deleted ones
    Page<Post> findByDeletedFalse(Pageable pageable);
    
    // Count posts by author
    long countByAuthorAndDeletedFalse(User author);
    
    // Find posts that need moderation (reported or flagged)
    @Query("{ 'deleted': false, $or: [ { 'upvotes': { $lt: -5 } }, { 'locked': true } ] }")
    Page<Post> findPostsNeedingModeration(Pageable pageable);
    
    // Get post with incremented view count
    @Query("{ '_id': ?0, 'deleted': false }")
    Optional<Post> findByIdAndDeletedFalse(String id);
    
    // Advanced search with multiple criteria
    @Query("{ " +
           "$and: [ " +
           "{ 'deleted': false }, " +
           "{ $or: [ " +
           "  { 'title': { $regex: ?0, $options: 'i' } }, " +
           "  { 'body': { $regex: ?0, $options: 'i' } }, " +
           "  { 'tags': { $in: [?0] } } " +
           "] } " +
           "] }")
    Page<Post> advancedSearch(String searchTerm, Pageable pageable);
}
package com.devforum.backend.repository;

import com.devforum.backend.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends MongoRepository<Tag, String> {
    
    Optional<Tag> findByName(String name);
    
    boolean existsByName(String name);
    
    // Find tags by name pattern
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Tag> findByNameContainingIgnoreCase(String namePart);
    
    // Find popular tags (ordered by post count)
    @Query(value = "{}", sort = "{ 'postCount': -1 }")
    Page<Tag> findPopularTags(Pageable pageable);
    
    // Find tags with minimum post count
    @Query("{ 'postCount': { $gte: ?0 } }")
    List<Tag> findTagsWithMinimumPosts(Integer minPostCount);
    
    // Search tags
    @Query("{ $or: [ " +
           "{ 'name': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } } ] }")
    Page<Tag> searchTags(String searchTerm, Pageable pageable);
    
    // Get trending tags (tags with recent growth)
    @Query(value = "{ 'postCount': { $gte: 1 } }", sort = "{ 'updatedAt': -1, 'postCount': -1 }")
    Page<Tag> findTrendingTags(Pageable pageable);
    
    // Find tags by names (for bulk operations)
    List<Tag> findByNameIn(List<String> names);
    
    // Count total tags
    @Override
    long count();
}
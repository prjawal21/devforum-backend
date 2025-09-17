package com.devforum.backend.repository;

import com.devforum.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    // Find users by role
    Page<User> findByRole(User.Role role, Pageable pageable);
    
    // Find users by reputation range
    @Query("{ 'reputation': { $gte: ?0, $lte: ?1 } }")
    Page<User> findByReputationBetween(Integer minReputation, Integer maxReputation, Pageable pageable);
    
    // Find recently active users
    @Query("{ 'lastLoginAt': { $gte: ?0 } }")
    List<User> findRecentlyActiveUsers(LocalDateTime since);
    
    // Search users by username or name
    @Query("{ $or: [ " +
           "{ 'username': { $regex: ?0, $options: 'i' } }, " +
           "{ 'firstName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'lastName': { $regex: ?0, $options: 'i' } } ] }")
    Page<User> searchUsers(String searchTerm, Pageable pageable);
    
    // Count users by role
    long countByRole(User.Role role);
    
    // Find top users by reputation
    @Query(value = "{}", sort = "{ 'reputation': -1 }")
    Page<User> findTopUsersByReputation(Pageable pageable);
}
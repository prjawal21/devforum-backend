package com.devforum.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

@Slf4j
@Configuration
public class MongoConfig {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Bean
    public CommandLineRunner createIndexes() {
        return args -> {
            try {
                log.info("Creating MongoDB indexes for performance optimization...");
                
                // User indexes
                createUserIndexes();
                
                // Post indexes
                createPostIndexes();
                
                // Comment indexes
                createCommentIndexes();
                
                // Vote indexes
                createVoteIndexes();
                
                log.info("MongoDB indexes created successfully!");
            } catch (Exception e) {
                log.error("Error creating MongoDB indexes: {}", e.getMessage());
            }
        };
    }
    
    private void createUserIndexes() {
        // Username index (unique)
        mongoTemplate.indexOps("users").ensureIndex(
            new Index("username", Sort.Direction.ASC).unique()
        );
        
        // Email index (unique)
        mongoTemplate.indexOps("users").ensureIndex(
            new Index("email", Sort.Direction.ASC).unique()
        );
        
        // Reputation index for leaderboards
        mongoTemplate.indexOps("users").ensureIndex(
            new Index("reputation", Sort.Direction.DESC)
        );
        
        // Role index for admin queries
        mongoTemplate.indexOps("users").ensureIndex(
            new Index("role", Sort.Direction.ASC)
        );
        
        // Created date index
        mongoTemplate.indexOps("users").ensureIndex(
            new Index("createdAt", Sort.Direction.DESC)
        );
        
        log.info("User indexes created");
    }
    
    private void createPostIndexes() {
        // Compound index for non-deleted posts sorted by creation date
        mongoTemplate.indexOps("posts").ensureIndex(
            new Index()
                .on("deleted", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
        );
        
        // Index for sorting by votes
        mongoTemplate.indexOps("posts").ensureIndex(
            new Index("upvotes", Sort.Direction.DESC)
        );
        
        // Index for last activity (trending posts)
        mongoTemplate.indexOps("posts").ensureIndex(
            new Index("lastActivityAt", Sort.Direction.DESC)
        );
        
        // Index for author queries
        mongoTemplate.indexOps("posts").ensureIndex(
            new Index("author", Sort.Direction.ASC)
        );
        
        // Index for tags
        mongoTemplate.indexOps("posts").ensureIndex(
            new Index("tags", Sort.Direction.ASC)
        );
        
        // Text index for full-text search on title and body
        TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("title", 3f)  // Higher weight for title
                .onField("body", 1f)   // Normal weight for body
                .onField("tags", 2f)   // Medium weight for tags
                .build();
        
        mongoTemplate.indexOps("posts").ensureIndex(textIndex);
        
        // Compound index for pinned posts
        mongoTemplate.indexOps("posts").ensureIndex(
            new Index()
                .on("pinned", Sort.Direction.DESC)
                .on("createdAt", Sort.Direction.DESC)
        );
        
        log.info("Post indexes created");
    }
    
    private void createCommentIndexes() {
        // Index for comments by post
        mongoTemplate.indexOps("comments").ensureIndex(
            new Index()
                .on("post", Sort.Direction.ASC)
                .on("deleted", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.ASC)
        );
        
        // Index for nested comments (parent-child relationship)
        mongoTemplate.indexOps("comments").ensureIndex(
            new Index("parentComment", Sort.Direction.ASC)
        );
        
        // Index for comment level (nesting depth)
        mongoTemplate.indexOps("comments").ensureIndex(
            new Index("level", Sort.Direction.ASC)
        );
        
        // Index for comments by author
        mongoTemplate.indexOps("comments").ensureIndex(
            new Index()
                .on("author", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
        );
        
        // Index for sorting by votes
        mongoTemplate.indexOps("comments").ensureIndex(
            new Index("upvotes", Sort.Direction.DESC)
        );
        
        log.info("Comment indexes created");
    }
    
    private void createVoteIndexes() {
        // Compound unique index to prevent duplicate votes
        mongoTemplate.indexOps("votes").ensureIndex(
            new Index()
                .on("user", Sort.Direction.ASC)
                .on("targetType", Sort.Direction.ASC)
                .on("targetId", Sort.Direction.ASC)
                .unique()
        );
        
        // Index for querying votes by target
        mongoTemplate.indexOps("votes").ensureIndex(
            new Index()
                .on("targetType", Sort.Direction.ASC)
                .on("targetId", Sort.Direction.ASC)
        );
        
        // Index for user's votes
        mongoTemplate.indexOps("votes").ensureIndex(
            new Index("user", Sort.Direction.ASC)
        );
        
        log.info("Vote indexes created");
    }
}
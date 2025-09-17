package com.devforum.backend.service;

import com.devforum.backend.dto.CreatePostRequest;
import com.devforum.backend.dto.PostDTO;
import com.devforum.backend.dto.UpdatePostRequest;
import com.devforum.backend.entity.Post;
import com.devforum.backend.entity.User;
import com.devforum.backend.entity.Vote;
import com.devforum.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    
    private final PostRepository postRepository;
    private final UserService userService;
    private final VoteService voteService;
    
    @Transactional
    public PostDTO createPost(CreatePostRequest request) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        Post post = Post.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .author(currentUser)
                .tags(request.getTags())
                .upvotes(0)
                .downvotes(0)
                .commentCount(0)
                .viewCount(0)
                .pinned(false)
                .locked(false)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .lastActivityAt(LocalDateTime.now())
                .build();
        
        Post savedPost = postRepository.save(post);
        log.info("Post created: {} by {}", savedPost.getTitle(), currentUser.getUsername());
        
        return PostDTO.fromPost(savedPost);
    }
    
    public PostDTO getPost(String postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        // Increment view count
        incrementViewCount(postId);
        
        // Get user's vote if authenticated
        String userVote = voteService.getUserVote(postId, Vote.TargetType.POST);
        
        return PostDTO.fromPostWithUserVote(post, userVote);
    }
    
    public Page<PostDTO> getPosts(Pageable pageable, String sortBy) {
        Page<Post> posts;
        
        switch (sortBy != null ? sortBy.toLowerCase() : "recent") {
            case "hot":
                posts = postRepository.findHotPosts(
                    LocalDateTime.now().minusDays(7), 1, pageable);
                break;
            case "top":
                posts = postRepository.findTopPosts(pageable);
                break;
            case "trending":
                posts = postRepository.findTrendingPosts(
                    LocalDateTime.now().minusDays(1), pageable);
                break;
            case "recent":
            default:
                posts = postRepository.findByDeletedFalse(pageable);
                break;
        }
        
        return posts.map(post -> {
            String userVote = voteService.getUserVote(post.getId(), Vote.TargetType.POST);
            return PostDTO.fromPostWithUserVote(post, userVote);
        });
    }
    
    public Page<PostDTO> getPostsByAuthor(String username, Pageable pageable) {
        User author = userService.getUserProfile(username) != null ? 
            userService.getCurrentUser().orElse(null) : null;
        
        if (author == null) {
            throw new RuntimeException("User not found: " + username);
        }
        
        Page<Post> posts = postRepository.findByAuthorAndDeletedFalse(author, pageable);
        return posts.map(post -> {
            String userVote = voteService.getUserVote(post.getId(), Vote.TargetType.POST);
            return PostDTO.fromPostWithUserVote(post, userVote);
        });
    }
    
    public Page<PostDTO> getPostsByTag(String tag, Pageable pageable) {
        Page<Post> posts = postRepository.findByTagAndDeletedFalse(tag, pageable);
        return posts.map(post -> {
            String userVote = voteService.getUserVote(post.getId(), Vote.TargetType.POST);
            return PostDTO.fromPostWithUserVote(post, userVote);
        });
    }
    
    public Page<PostDTO> searchPosts(String query, Pageable pageable) {
        Page<Post> posts = postRepository.advancedSearch(query, pageable);
        return posts.map(post -> {
            String userVote = voteService.getUserVote(post.getId(), Vote.TargetType.POST);
            return PostDTO.fromPostWithUserVote(post, userVote);
        });
    }
    
    @Transactional
    public PostDTO updatePost(String postId, UpdatePostRequest request) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        // Check if user is the author or has permission
        if (!post.getAuthor().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.MODERATOR && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied: You can only edit your own posts");
        }
        
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            post.setBody(request.getBody());
        }
        if (request.getTags() != null) {
            post.setTags(request.getTags());
        }
        
        post.setUpdatedAt(LocalDateTime.now());
        
        Post updatedPost = postRepository.save(post);
        log.info("Post updated: {} by {}", updatedPost.getTitle(), currentUser.getUsername());
        
        String userVote = voteService.getUserVote(postId, Vote.TargetType.POST);
        return PostDTO.fromPostWithUserVote(updatedPost, userVote);
    }
    
    @Transactional
    public void deletePost(String postId) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        // Check if user is the author or has permission
        if (!post.getAuthor().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != User.Role.MODERATOR && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied: You can only delete your own posts");
        }
        
        post.setDeleted(true);
        post.setUpdatedAt(LocalDateTime.now());
        
        postRepository.save(post);
        log.info("Post deleted: {} by {}", post.getTitle(), currentUser.getUsername());
    }
    
    @Transactional
    public PostDTO pinPost(String postId) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        if (currentUser.getRole() != User.Role.MODERATOR && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied: Moderator or Admin role required");
        }
        
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        post.setPinned(!post.getPinned()); // Toggle pin status
        post.setUpdatedAt(LocalDateTime.now());
        
        Post updatedPost = postRepository.save(post);
        log.info("Post pin status changed: {} -> {} by {}", 
            updatedPost.getTitle(), updatedPost.getPinned(), currentUser.getUsername());
        
        String userVote = voteService.getUserVote(postId, Vote.TargetType.POST);
        return PostDTO.fromPostWithUserVote(updatedPost, userVote);
    }
    
    @Transactional
    public PostDTO lockPost(String postId) {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        
        if (currentUser.getRole() != User.Role.MODERATOR && 
            currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Access denied: Moderator or Admin role required");
        }
        
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new RuntimeException("Post not found: " + postId));
        
        post.setLocked(!post.getLocked()); // Toggle lock status
        post.setUpdatedAt(LocalDateTime.now());
        
        Post updatedPost = postRepository.save(post);
        log.info("Post lock status changed: {} -> {} by {}", 
            updatedPost.getTitle(), updatedPost.getLocked(), currentUser.getUsername());
        
        String userVote = voteService.getUserVote(postId, Vote.TargetType.POST);
        return PostDTO.fromPostWithUserVote(updatedPost, userVote);
    }
    
    public List<PostDTO> getPinnedPosts() {
        List<Post> posts = postRepository.findPinnedPosts();
        return posts.stream()
                .map(post -> {
                    String userVote = voteService.getUserVote(post.getId(), Vote.TargetType.POST);
                    return PostDTO.fromPostWithUserVote(post, userVote);
                })
                .toList();
    }
    
    @Transactional
    protected void incrementViewCount(String postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        });
    }
}
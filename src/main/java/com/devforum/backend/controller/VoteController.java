package com.devforum.backend.controller;

import com.devforum.backend.dto.VoteRequest;
import com.devforum.backend.entity.Vote;
import com.devforum.backend.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {
    
    private final VoteService voteService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> vote(@Valid @RequestBody VoteRequest request) {
        try {
            Vote.VoteType voteType = Vote.VoteType.valueOf(request.getVoteType());
            Vote.TargetType targetType = Vote.TargetType.valueOf(request.getTargetType());
            
            String result = voteService.vote(request.getTargetId(), targetType, voteType);
            
            return ResponseEntity.ok(Map.of(
                "message", "Vote processed successfully",
                "action", result,
                "voteType", request.getVoteType(),
                "targetId", request.getTargetId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<Map<String, String>> getUserVote(
            @PathVariable String targetType,
            @PathVariable String targetId) {
        try {
            Vote.TargetType type = Vote.TargetType.valueOf(targetType.toUpperCase());
            String userVote = voteService.getUserVote(targetId, type);
            
            if (userVote != null) {
                return ResponseEntity.ok(Map.of("vote", userVote));
            } else {
                return ResponseEntity.ok(Map.of("vote", "none"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
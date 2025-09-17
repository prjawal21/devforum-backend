package com.devforum.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.ArrayList;

@Data
public class CreatePostRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @NotBlank(message = "Body is required")
    @Size(min = 10, max = 50000, message = "Body must be between 10 and 50000 characters")
    private String body;
    
    private List<String> tags = new ArrayList<>();
}
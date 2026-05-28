package com.nutrition.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private String message;
    
    public AuthResponse(String token, Long userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }
    
    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
}
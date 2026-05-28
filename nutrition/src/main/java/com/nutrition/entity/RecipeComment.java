package com.nutrition.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class RecipeComment {
    private Long id;
    private Long recipeId;
    private Long userId;
    private String username;
    private String content;
    private Timestamp createdAt;
}

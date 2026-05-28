package com.nutrition.entity;

import lombok.Data;

@Data
public class RecipeStep {
    private Long id;
    private Long recipeId;
    private Integer stepOrder;
    private String description;
    private String imageUrl;
    private Boolean isKey;
}

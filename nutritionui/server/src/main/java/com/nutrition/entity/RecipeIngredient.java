package com.nutrition.entity;

import lombok.Data;

@Data
public class RecipeIngredient {
    private Long id;
    private Long recipeId;
    private String type; // main/aux
    private String name;
    private String amount;
}

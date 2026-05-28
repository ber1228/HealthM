package com.nutrition.mapper;

import com.nutrition.entity.RecipeIngredient;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecipeIngredientMapper {

    @Insert("INSERT INTO recipe_ingredients (recipe_id, type, name, amount) VALUES (#{recipeId}, #{type}, #{name}, #{amount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RecipeIngredient ingredient);

    @Delete("DELETE FROM recipe_ingredients WHERE recipe_id=#{recipeId}")
    int deleteByRecipeId(Long recipeId);

    @Select("SELECT * FROM recipe_ingredients WHERE recipe_id=#{recipeId} ORDER BY id ASC")
    List<RecipeIngredient> findByRecipeId(Long recipeId);
}

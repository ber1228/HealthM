package com.nutrition.mapper;

import com.nutrition.entity.RecipeStep;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecipeStepMapper {

    @Insert("INSERT INTO recipe_steps (recipe_id, step_order, description, image_url, is_key) VALUES (#{recipeId}, #{stepOrder}, #{description}, #{imageUrl}, #{isKey})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RecipeStep step);

    @Delete("DELETE FROM recipe_steps WHERE recipe_id=#{recipeId}")
    int deleteByRecipeId(Long recipeId);

    @Select("SELECT * FROM recipe_steps WHERE recipe_id=#{recipeId} ORDER BY step_order ASC")
    List<RecipeStep> findByRecipeId(Long recipeId);
}

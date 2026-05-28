package com.nutrition.mapper;

import com.nutrition.entity.RecipeComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecipeCommentMapper {

    @Insert("INSERT INTO recipe_comments (recipe_id, user_id, username, content) VALUES (#{recipeId}, #{userId}, #{username}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RecipeComment comment);

    @Select("SELECT * FROM recipe_comments WHERE recipe_id=#{recipeId} ORDER BY created_at DESC LIMIT #{limit}")
    List<RecipeComment> findByRecipeId(@Param("recipeId") Long recipeId, @Param("limit") int limit);

    @Delete("DELETE FROM recipe_comments WHERE id=#{id}")
    int deleteById(Long id);
}

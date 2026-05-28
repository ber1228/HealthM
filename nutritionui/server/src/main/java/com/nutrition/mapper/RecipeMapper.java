package com.nutrition.mapper;

import com.nutrition.entity.Recipe;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecipeMapper {

    @Insert("INSERT INTO recipes (name, author, category, duration_minutes, difficulty, taste, cover_image_url, calories, published) " +
            "VALUES (#{name}, #{author}, #{category}, #{durationMinutes}, #{difficulty}, #{taste}, #{coverImageUrl}, #{calories}, #{published})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Recipe recipe);

    @Update("UPDATE recipes SET name=#{name}, author=#{author}, category=#{category}, duration_minutes=#{durationMinutes}, " +
            "difficulty=#{difficulty}, taste=#{taste}, cover_image_url=#{coverImageUrl}, calories=#{calories}, published=#{published} " +
            "WHERE id=#{id}")
    int update(Recipe recipe);

    @Delete("DELETE FROM recipes WHERE id=#{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM recipes WHERE id = #{id}")
    Recipe findById(Long id);

    @Select({
            "<script>",
            "SELECT * FROM recipes",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (name LIKE CONCAT('%', #{keyword}, '%') OR author LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='category != null and category != \"\"'>",
            "    AND category = #{category}",
            "  </if>",
            "  <if test='difficulty != null and difficulty != \"\"'>",
            "    AND difficulty = #{difficulty}",
            "  </if>",
            "  <if test='taste != null and taste != \"\"'>",
            "    AND taste = #{taste}",
            "  </if>",
            "  <if test='minDuration != null'>",
            "    AND duration_minutes &gt;= #{minDuration}",
            "  </if>",
            "  <if test='maxDuration != null'>",
            "    AND duration_minutes &lt;= #{maxDuration}",
            "  </if>",
            "  <if test='published != null'>",
            "    AND published = #{published}",
            "  </if>",
            "</where>",
            "ORDER BY (published IS NULL), published DESC, id DESC",
            "LIMIT #{limit} OFFSET #{offset}",
            "</script>"
    })
    List<Recipe> page(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("taste") String taste,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration,
            @Param("published") Boolean published,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    @Select({
            "<script>",
            "SELECT COUNT(*) FROM recipes",
            "<where>",
            "  <if test='keyword != null and keyword != \"\"'>",
            "    AND (name LIKE CONCAT('%', #{keyword}, '%') OR author LIKE CONCAT('%', #{keyword}, '%'))",
            "  </if>",
            "  <if test='category != null and category != \"\"'>",
            "    AND category = #{category}",
            "  </if>",
            "  <if test='difficulty != null and difficulty != \"\"'>",
            "    AND difficulty = #{difficulty}",
            "  </if>",
            "  <if test='taste != null and taste != \"\"'>",
            "    AND taste = #{taste}",
            "  </if>",
            "  <if test='minDuration != null'>",
            "    AND duration_minutes &gt;= #{minDuration}",
            "  </if>",
            "  <if test='maxDuration != null'>",
            "    AND duration_minutes &lt;= #{maxDuration}",
            "  </if>",
            "  <if test='published != null'>",
            "    AND published = #{published}",
            "  </if>",
            "</where>",
            "</script>"
    })
    int count(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("difficulty") String difficulty,
            @Param("taste") String taste,
            @Param("minDuration") Integer minDuration,
            @Param("maxDuration") Integer maxDuration,
            @Param("published") Boolean published
    );
}

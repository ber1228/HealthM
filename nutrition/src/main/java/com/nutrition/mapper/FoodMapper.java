package com.nutrition.mapper;

import com.nutrition.entity.Food;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FoodMapper {
    
    @Select("SELECT * FROM foods WHERE id = #{id}")
    Food findById(Long id);
    
    @Select("SELECT * FROM foods WHERE name LIKE CONCAT('%', #{keyword}, '%')")
    List<Food> searchByName(String keyword);
    
    @Select("SELECT * FROM foods WHERE category = #{category}")
    List<Food> findByCategory(String category);
    
    @Select("SELECT * FROM foods")
    List<Food> findAll();

    @Select("SELECT * FROM foods WHERE (availability IS NULL OR availability != 'disabled')")
    List<Food> findAllEnabled();

    @Select("SELECT * FROM foods WHERE (availability IS NULL OR availability != 'disabled') AND category = #{category}")
    List<Food> findByCategoryEnabled(String category);

    @Select("SELECT * FROM foods WHERE (availability IS NULL OR availability != 'disabled') AND name LIKE CONCAT('%', #{keyword}, '%')")
    List<Food> searchByNameEnabled(String keyword);
    
    @Select("SELECT * FROM foods WHERE calories BETWEEN #{minCal} AND #{maxCal} AND category = #{category}")
    List<Food> findSimilarFoods(@Param("minCal") Double minCal, @Param("maxCal") Double maxCal, @Param("category") String category);

    @Select({
        "<script>",
        "SELECT * FROM foods",
        "<where>",
        "  <if test='keyword != null and keyword != \\\"\\\"'>",
        "    AND name LIKE CONCAT('%', #{keyword}, '%')",
        "  </if>",
        "  <if test='category != null and category != \\\"\\\"'>",
        "    AND category = #{category}",
        "  </if>",
        "</where>",
        "ORDER BY id DESC",
        "LIMIT #{limit} OFFSET #{offset}",
        "</script>"
    })
    List<Food> queryFoods(@Param("keyword") String keyword,
                          @Param("category") String category,
                          @Param("offset") Integer offset,
                          @Param("limit") Integer limit);

    @Select({
        "<script>",
        "SELECT * FROM foods",
        "<where>",
        "  (availability IS NULL OR availability != 'disabled')",
        "  <if test='keyword != null and keyword != \\\"\\\"'>",
        "    AND name LIKE CONCAT('%', #{keyword}, '%')",
        "  </if>",
        "  <if test='category != null and category != \\\"\\\"'>",
        "    AND category = #{category}",
        "  </if>",
        "</where>",
        "ORDER BY id DESC",
        "LIMIT #{limit} OFFSET #{offset}",
        "</script>"
    })
    List<Food> queryFoodsEnabled(@Param("keyword") String keyword,
                                 @Param("category") String category,
                                 @Param("offset") Integer offset,
                                 @Param("limit") Integer limit);

    @Select({
        "<script>",
        "SELECT COUNT(*) FROM foods",
        "<where>",
        "  <if test='keyword != null and keyword != \\\"\\\"'>",
        "    AND name LIKE CONCAT('%', #{keyword}, '%')",
        "  </if>",
        "  <if test='category != null and category != \\\"\\\"'>",
        "    AND category = #{category}",
        "  </if>",
        "</where>",
        "</script>"
    })
    int countFoods(@Param("keyword") String keyword,
                   @Param("category") String category);

    @Select({
        "<script>",
        "SELECT COUNT(*) FROM foods",
        "<where>",
        "  (availability IS NULL OR availability != 'disabled')",
        "  <if test='keyword != null and keyword != \\\"\\\"'>",
        "    AND name LIKE CONCAT('%', #{keyword}, '%')",
        "  </if>",
        "  <if test='category != null and category != \\\"\\\"'>",
        "    AND category = #{category}",
        "  </if>",
        "</where>",
        "</script>"
    })
    int countFoodsEnabled(@Param("keyword") String keyword,
                          @Param("category") String category);

    @Select("SELECT * FROM foods ORDER BY (protein IS NULL), protein DESC, id DESC LIMIT #{limit}")
    List<Food> topByProtein(@Param("limit") int limit);

    @Select("SELECT * FROM foods WHERE (availability IS NULL OR availability != 'disabled') ORDER BY (protein IS NULL), protein DESC, id DESC LIMIT #{limit}")
    List<Food> topByProteinEnabled(@Param("limit") int limit);

    @Insert("INSERT INTO foods (name, category, calories, protein, carbs, fat, fiber, calcium, iron, vitc, potassium, sodium, season, availability, image_url) " +
            "VALUES (#{name}, #{category}, #{calories}, #{protein}, #{carbs}, #{fat}, #{fiber}, #{calcium}, #{iron}, #{vitc}, #{potassium}, #{sodium}, #{season}, #{availability}, #{imageUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Food food);

    @Update("UPDATE foods SET name=#{name}, category=#{category}, calories=#{calories}, protein=#{protein}, carbs=#{carbs}, fat=#{fat}, " +
            "fiber=#{fiber}, calcium=#{calcium}, iron=#{iron}, vitc=#{vitc}, potassium=#{potassium}, sodium=#{sodium}, " +
            "season=#{season}, availability=#{availability}, image_url=#{imageUrl} WHERE id=#{id}")
    int update(Food food);

    @Delete("DELETE FROM foods WHERE id=#{id}")
    int deleteById(Long id);
}


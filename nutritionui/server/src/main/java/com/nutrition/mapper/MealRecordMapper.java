package com.nutrition.mapper;

import com.nutrition.entity.MealRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MealRecordMapper {
    
    @Insert("INSERT INTO meal_records (user_id, date, meal_type, food_id, food_name, amount, calories, protein, carbs, fat, " +
            "fiber, calcium, iron, vitc, potassium, sodium) VALUES (#{userId}, #{date}, #{mealType}, #{foodId}, #{foodName}, " +
            "#{amount}, #{calories}, #{protein}, #{carbs}, #{fat}, #{fiber}, #{calcium}, #{iron}, #{vitc}, #{potassium}, #{sodium})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MealRecord record);
    
    @Select("SELECT * FROM meal_records WHERE user_id = #{userId} AND date = #{date}")
    List<MealRecord> findByUserAndDate(@Param("userId") Long userId, @Param("date") java.sql.Date date);
    
    @Select("SELECT * FROM meal_records WHERE user_id = #{userId} AND date BETWEEN #{startDate} AND #{endDate}")
    List<MealRecord> findByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate);
    
    @Delete("DELETE FROM meal_records WHERE id = #{id}")
    int delete(Long id);
}

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

    @Select("SELECT * FROM meal_records " +
            "WHERE user_id = #{userId} " +
            "AND DATE(date) = DATE(#{date}) " +
            "ORDER BY " +
            "CASE meal_type " +
            "WHEN 'breakfast' THEN 1 " +
            "WHEN 'morning_snack' THEN 2 " +
            "WHEN 'lunch' THEN 3 " +
            "WHEN 'afternoon_snack' THEN 4 " +
            "WHEN 'dinner' THEN 5 " +
            "WHEN 'evening_snack' THEN 6 " +
            "ELSE 7 " +
            "END, " +
            "created_at")
    List<MealRecord> findByUserAndDate(@Param("userId") Long userId, @Param("date") java.sql.Date date);

    @Select("SELECT * FROM meal_records WHERE user_id = #{userId} AND date BETWEEN #{startDate} AND #{endDate}")
    List<MealRecord> findByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate);

    @Delete("DELETE FROM meal_records WHERE id = #{id}")
    int delete(Long id);

    @Insert("<script>" +
            "INSERT INTO meal_records (user_id, date, meal_type, food_id, food_name, amount, " +
            "calories, protein, carbs, fat, fiber, calcium, iron, vitc, potassium, sodium) VALUES " +
            "<foreach collection='records' item='r' separator=','>" +
            "(#{r.userId}, #{r.date}, #{r.mealType}, #{r.foodId}, #{r.foodName}, #{r.amount}, " +
            "#{r.calories}, #{r.protein}, #{r.carbs}, #{r.fat}, #{r.fiber}, #{r.calcium}, #{r.iron}, " +
            "#{r.vitc}, #{r.potassium}, #{r.sodium})" +
            "</foreach></script>")
    int batchInsert(@Param("records") List<MealRecord> records);
}

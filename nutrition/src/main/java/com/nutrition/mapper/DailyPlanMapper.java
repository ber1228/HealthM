package com.nutrition.mapper;

import com.nutrition.entity.DailyPlan;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DailyPlanMapper {
    
    @Insert("INSERT INTO daily_plans (user_id, date, meal_type, food_id, food_name, amount, calories, protein, carbs, fat) " +
            "VALUES (#{userId}, #{date}, #{mealType}, #{foodId}, #{foodName}, #{amount}, #{calories}, #{protein}, #{carbs}, #{fat})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DailyPlan plan);
    
    @Select("SELECT id, user_id as userId, date, meal_type as mealType, food_id as foodId, " +
            "food_name as foodName, amount, calories, protein, carbs, fat, created_at as createdAt " +
            "FROM daily_plans WHERE user_id = #{userId} AND date = #{date}")
    List<DailyPlan> findByUserAndDate(@Param("userId") Long userId, @Param("date") java.sql.Date date);
    
    @Delete("DELETE FROM daily_plans WHERE id = #{id}")
    int delete(Long id);
    
    @Update("UPDATE daily_plans SET food_id=#{foodId}, food_name=#{foodName}, amount=#{amount}, " +
            "calories=#{calories}, protein=#{protein}, carbs=#{carbs}, fat=#{fat} WHERE id=#{id}")
    int update(DailyPlan plan);
    
    @Select("SELECT id, user_id as userId, date, meal_type as mealType, food_id as foodId, " +
            "food_name as foodName, amount, calories, protein, carbs, fat, created_at as createdAt " +
            "FROM daily_plans WHERE id = #{id}")
    DailyPlan findById(Long id);
    
    @Delete("DELETE FROM daily_plans WHERE user_id = #{userId} AND date = #{date}")
    int deleteByUserAndDate(@Param("userId") Long userId, @Param("date") java.sql.Date date);
    
    @Select("SELECT id, user_id as userId, date, meal_type as mealType, food_id as foodId, " +
            "food_name as foodName, amount, calories, protein, carbs, fat, created_at as createdAt " +
            "FROM daily_plans WHERE user_id = #{userId} AND date >= #{weekStart} AND date < DATE_ADD(#{weekStart}, INTERVAL 7 DAY)")
    List<DailyPlan> findByUserAndWeekStart(@Param("userId") Long userId, @Param("weekStart") java.sql.Date weekStart);
}


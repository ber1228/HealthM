package com.nutrition.mapper;

import com.nutrition.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    
    @Insert("INSERT INTO users (username, password, role, phone, email, age, gender, height, weight, activity_level, health_goal, bmr, daily_calories, goal_start_date, initial_weight, target_value) " +
            "VALUES (#{username}, #{password}, #{role}, #{phone}, #{email}, #{age}, #{gender}, #{height}, #{weight}, #{activityLevel}, #{healthGoal}, #{bmr}, #{dailyCalories}, #{goalStartDate}, #{initialWeight}, #{targetValue})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT id, username, role, phone, email, age, gender, height, weight, " +
            "activity_level as activityLevel, health_goal as healthGoal, bmr, " +
            "daily_calories as dailyCalories, goal_start_date as goalStartDate, " +
            "initial_weight as initialWeight, target_value as targetValue, created_at as createdAt, updated_at as updatedAt " +
            "FROM users WHERE id = #{id}")
    User findById(Long id);
    
    @Update("UPDATE users SET phone=#{phone}, email=#{email}, age=#{age}, gender=#{gender}, height=#{height}, weight=#{weight}, " +
            "activity_level=#{activityLevel}, health_goal=#{healthGoal}, bmr=#{bmr}, daily_calories=#{dailyCalories}, " +
            "goal_start_date=#{goalStartDate}, initial_weight=#{initialWeight}, target_value=#{targetValue} WHERE id=#{id}")
    int update(User user);
    
    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> findAll();
    
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findByIdWithRole(Long id);
    
    @Update("UPDATE users SET role=#{role} WHERE id=#{id}")
    int updateRole(@Param("id") Long id, @Param("role") String role);
    
    @Delete("DELETE FROM users WHERE id=#{id}")
    int deleteById(Long id);
}


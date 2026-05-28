package com.nutrition.mapper;

import com.nutrition.entity.WeeklyPlan;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WeeklyPlanMapper {
    
    @Insert("INSERT INTO weekly_plans (user_id, week_start, is_template, template_name, created_at) " +
            "VALUES (#{userId}, #{weekStart}, #{isTemplate}, #{templateName}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(WeeklyPlan weeklyPlan);
    
    @Select("SELECT * FROM weekly_plans WHERE id = #{id}")
    WeeklyPlan findById(Long id);
    
    @Select("SELECT * FROM weekly_plans WHERE user_id = #{userId} AND is_template = true")
    List<WeeklyPlan> findTemplatesByUser(Long userId);
    
    @Select("SELECT * FROM weekly_plans WHERE user_id = #{userId} AND week_start = #{weekStart} ORDER BY created_at DESC LIMIT 1")
    WeeklyPlan findByUserAndWeek(Long userId, java.sql.Date weekStart);
    
    @Update("UPDATE weekly_plans SET template_name = #{templateName} WHERE id = #{id}")
    void updateTemplateName(Long id, String templateName);
    
    @Delete("DELETE FROM weekly_plans WHERE id = #{id}")
    void deleteById(Long id);
}


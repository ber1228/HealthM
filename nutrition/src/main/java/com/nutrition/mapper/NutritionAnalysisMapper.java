package com.nutrition.mapper;

import com.nutrition.entity.NutritionAnalysis;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NutritionAnalysisMapper {
    
    @Insert("INSERT INTO nutrition_analysis (user_id, date, analysis_type, total_calories, total_protein, total_carbs, total_fat, " +
            "total_fiber, total_calcium, total_iron, total_vitc, total_potassium, total_sodium, target_calories, target_protein, " +
            "target_carbs, target_fat, analysis_result, gap_report) VALUES (#{userId}, #{date}, #{analysisType}, #{totalCalories}, " +
            "#{totalProtein}, #{totalCarbs}, #{totalFat}, #{totalFiber}, #{totalCalcium}, #{totalIron}, #{totalVitc}, " +
            "#{totalPotassium}, #{totalSodium}, #{targetCalories}, #{targetProtein}, #{targetCarbs}, #{targetFat}, #{analysisResult}, #{gapReport})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NutritionAnalysis analysis);
    
    @Select("SELECT * FROM nutrition_analysis WHERE user_id = #{userId} AND date = #{date} AND analysis_type = #{type}")
    NutritionAnalysis findByUserAndDateAndType(@Param("userId") Long userId, @Param("date") java.sql.Date date, @Param("type") String type);
    
    @Select("SELECT * FROM nutrition_analysis WHERE user_id = #{userId} AND date BETWEEN #{startDate} AND #{endDate}")
    List<NutritionAnalysis> findByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate);
    
    // 获取每个日期最新的分析记录
    @Select("SELECT n1.* FROM nutrition_analysis n1 " +
            "INNER JOIN (" +
            "  SELECT user_id, date, MAX(created_at) as max_created " +
            "  FROM nutrition_analysis " +
            "  WHERE user_id = #{userId} AND date BETWEEN #{startDate} AND #{endDate} " +
            "  GROUP BY user_id, date" +
            ") n2 ON n1.user_id = n2.user_id AND n1.date = n2.date AND n1.created_at = n2.max_created " +
            "ORDER BY n1.date DESC")
    List<NutritionAnalysis> findLatestByUserAndDateRange(@Param("userId") Long userId, @Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate);
}


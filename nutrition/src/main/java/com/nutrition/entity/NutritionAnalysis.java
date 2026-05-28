package com.nutrition.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
public class NutritionAnalysis {
    private Long id;
    private Long userId;
    private Date date;
    private String analysisType; // plan/report
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFat;
    private BigDecimal totalFiber;
    private BigDecimal totalCalcium;
    private BigDecimal totalIron;
    private BigDecimal totalVitc;
    private BigDecimal totalPotassium;
    private BigDecimal totalSodium;
    private BigDecimal targetCalories;
    private BigDecimal targetProtein;
    private BigDecimal targetCarbs;
    private BigDecimal targetFat;
    private String analysisResult;
    private String gapReport;
    private Timestamp createdAt;
}


package com.nutrition.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
public class DailyPlan {
    private Long id;
    private Long userId;
    private Date date;
    private String mealType; // breakfast, lunch, dinner, morning_snack, afternoon_snack, evening_snack
    private Long foodId;
    private String foodName;
    private BigDecimal amount;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private Timestamp createdAt;
}


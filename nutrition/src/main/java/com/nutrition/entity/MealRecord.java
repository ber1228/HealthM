package com.nutrition.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
public class MealRecord {
    private Long id;
    private Long userId;
    private Date date;
    private String mealType;
    private Long foodId;
    private String foodName;
    private BigDecimal amount;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private BigDecimal fiber;
    private BigDecimal calcium;
    private BigDecimal iron;
    private BigDecimal vitc;
    private BigDecimal potassium;
    private BigDecimal sodium;
    private Timestamp createdAt;
}


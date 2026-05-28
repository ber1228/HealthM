package com.nutrition.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class Food {
    private Long id;
    private String name;
    private String category;
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
    private String season;
    private String availability;
    private String imageUrl;
    private Timestamp createdAt;
}


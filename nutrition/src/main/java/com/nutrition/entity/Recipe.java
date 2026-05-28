package com.nutrition.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class Recipe {
    private Long id;
    private String name;
    private String author;
    private String category;
    private Integer durationMinutes;
    private String difficulty; // 简单/中等/困难
    private String taste; // 清淡/微辣/重口
    private String coverImageUrl;
    private BigDecimal calories;
    private Boolean published;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

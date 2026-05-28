package com.nutrition.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class User {
    private Long id;
    private String username;
    private String role; // ADMIN-管理员, USER-普通用户
    private String password;
    private String phone;
    private String email;
    private Integer age;
    private Integer gender; // 0:女, 1:男
    private BigDecimal height; // 身高(cm)
    private BigDecimal weight; // 体重(kg)
    private Integer activityLevel; // 1-久坐, 2-轻度, 3-中度, 4-高强度
    private String healthGoal; // 健康目标
    private BigDecimal bmr; // 基础代谢率
    private BigDecimal dailyCalories; // 每日推荐热量
    private Timestamp goalStartDate; // 目标开始日期
    private BigDecimal initialWeight; // 初始体重(用于计算减重进度)
    private BigDecimal targetValue; // 目标值(如减重目标、增肌目标等)
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

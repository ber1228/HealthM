package com.nutrition.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserInfoRequest {
    private String phone;
    private String email;
    private Integer age;
    private Integer gender;
    private BigDecimal height;
    private BigDecimal weight;
    private Integer activityLevel;
    private String healthGoal;
    private BigDecimal targetValue; // 目标值(如减重目标、增肌目标等)
}


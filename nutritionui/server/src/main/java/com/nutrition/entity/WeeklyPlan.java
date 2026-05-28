package com.nutrition.entity;

import lombok.Data;
import java.sql.Date;
import java.sql.Timestamp;

@Data
public class WeeklyPlan {
    private Long id;
    private Long userId;
    private Date weekStart;
    private Boolean isTemplate;
    private String templateName;
    private Timestamp createdAt;
}


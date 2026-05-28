package com.nutrition.entity;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class Message {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String messageType; // reminder/nutrition/target
    private Boolean isRead;
    private Timestamp createdAt;
}


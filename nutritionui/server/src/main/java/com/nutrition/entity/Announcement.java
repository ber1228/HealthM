package com.nutrition.entity;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class Announcement {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Boolean isPinned;
    private Timestamp publishTime;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}


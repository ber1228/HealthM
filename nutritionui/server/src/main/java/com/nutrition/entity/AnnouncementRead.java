package com.nutrition.entity;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class AnnouncementRead {
    private Long id;
    private Long announcementId;
    private Long userId;
    private Timestamp readAt;
}

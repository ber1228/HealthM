package com.nutrition.entity;

import java.sql.Time;

public class Reminder {
    private Long id;
    private Long userId;
    private String reminderType; // meal, nutrition, target
    private Time reminderTime;
    private String daysOfWeek; // 1,2,3,4,5,6,7
    private Boolean isEnabled;
    private String createdAt;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getReminderType() {
        return reminderType;
    }
    
    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }
    
    public Time getReminderTime() {
        return reminderTime;
    }
    
    public void setReminderTime(Time reminderTime) {
        this.reminderTime = reminderTime;
    }
    
    public String getDaysOfWeek() {
        return daysOfWeek;
    }
    
    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}


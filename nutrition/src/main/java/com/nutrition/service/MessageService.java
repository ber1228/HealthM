package com.nutrition.service;

import com.nutrition.entity.Message;
import com.nutrition.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    // 发送饮食提醒
    public void sendMealReminder(Long userId, String mealType, String message) {
        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setTitle("饮食提醒");
        reminder.setContent(message);
        reminder.setMessageType("reminder");
        reminder.setIsRead(false);
        reminder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        messageMapper.insert(reminder);
    }
    
    // 发送营养提醒
    public void sendNutritionReminder(Long userId, String nutrient, String message) {
        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setTitle("营养提醒 - " + nutrient);
        reminder.setContent(message);
        reminder.setMessageType("nutrition");
        reminder.setIsRead(false);
        reminder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        messageMapper.insert(reminder);
    }
    
    // 发送目标进度提醒
    public void sendTargetReminder(Long userId, String target, String message) {
        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setTitle("目标进度提醒 - " + target);
        reminder.setContent(message);
        reminder.setMessageType("target");
        reminder.setIsRead(false);
        reminder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        messageMapper.insert(reminder);
    }
    
    // 自动发送营养提醒（当检测到营养不足或超标时）
    public void sendAutoNutritionReminder(Long userId, String nutrient, String status, String suggestion) {
        String title = "营养" + status + "提醒";
        String content = String.format("您近期的%s摄入%s，建议%s", nutrient, status, suggestion);
        
        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setTitle(title);
        reminder.setContent(content);
        reminder.setMessageType("nutrition");
        reminder.setIsRead(false);
        reminder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        messageMapper.insert(reminder);
    }
    
    // 自动发送目标进度提醒
    public void sendAutoTargetReminder(Long userId, String healthGoal, String progress, String suggestion) {
        String title = "目标进度提醒";
        String content = String.format("您的%s目标进展：%s。%s", healthGoal, progress, suggestion);
        
        Message reminder = new Message();
        reminder.setUserId(userId);
        reminder.setTitle(title);
        reminder.setContent(content);
        reminder.setMessageType("target");
        reminder.setIsRead(false);
        reminder.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        messageMapper.insert(reminder);
    }
}


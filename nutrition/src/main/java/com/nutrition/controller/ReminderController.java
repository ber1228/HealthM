package com.nutrition.controller;

import com.nutrition.entity.Reminder;
import com.nutrition.mapper.ReminderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Time;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reminders")
@CrossOrigin
public class ReminderController {
    
    @Autowired
    private ReminderMapper reminderMapper;
    
    // 获取用户的所有提醒设置
    @GetMapping
    public ResponseEntity<?> getUserReminders(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Reminder> reminders = reminderMapper.findByUserId(userId);
            return ResponseEntity.ok(reminders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 创建提醒设置
    @PostMapping
    public ResponseEntity<?> createReminder(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            
            String timeStr = (String) request.get("reminderTime");
            if (timeStr != null && timeStr.length() == 5) {
                timeStr = timeStr + ":00"; // 补全秒数
            }
            
            Reminder reminder = new Reminder();
            reminder.setUserId(userId);
            reminder.setReminderType((String) request.get("reminderType"));
            reminder.setReminderTime(timeStr != null ? Time.valueOf(timeStr) : null);
            reminder.setDaysOfWeek((String) request.get("daysOfWeek"));
            reminder.setIsEnabled(true);
            
            reminderMapper.insert(reminder);
            return ResponseEntity.ok("提醒设置成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 更新提醒设置
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReminder(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Reminder reminder = reminderMapper.findById(id);
            if (reminder == null) {
                return ResponseEntity.badRequest().body("提醒不存在");
            }
            
            if (request.containsKey("reminderTime")) {
                String timeStr = (String) request.get("reminderTime");
                if (timeStr != null && timeStr.length() == 5) {
                    timeStr = timeStr + ":00"; // 补全秒数
                }
                reminder.setReminderTime(timeStr != null ? Time.valueOf(timeStr) : null);
            }
            if (request.containsKey("daysOfWeek")) {
                reminder.setDaysOfWeek((String) request.get("daysOfWeek"));
            }
            if (request.containsKey("isEnabled")) {
                reminder.setIsEnabled((Boolean) request.get("isEnabled"));
            }
            
            reminderMapper.update(reminder);
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 删除提醒设置
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReminder(@PathVariable Long id) {
        try {
            reminderMapper.delete(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


package com.nutrition.controller;

import com.nutrition.entity.Message;
import com.nutrition.mapper.MessageMapper;
import com.nutrition.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
@CrossOrigin
public class MessageController {
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private MessageService messageService;
    
    @GetMapping
    public ResponseEntity<?> getMessages(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Message> messages = messageMapper.findByUserId(userId, 50);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestMessages(@RequestParam(value = "limit", defaultValue = "5") int limit,
                                               HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Message> messages = messageMapper.findByUserId(userId, Math.min(Math.max(limit, 1), 50));
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadMessages(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<Message> messages = messageMapper.findUnreadByUserId(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            int count = messageMapper.countUnreadByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            messageMapper.markAsRead(id);
            return ResponseEntity.ok("已标记为已读");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            messageMapper.markAllAsRead(userId);
            return ResponseEntity.ok("已全部标记为已读");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        try {
            messageMapper.delete(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 发送饮食提醒
    @PostMapping("/meal-reminder")
    public ResponseEntity<?> sendMealReminder(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String mealType = (String) request.get("mealType");
            String message = (String) request.get("message");
            messageService.sendMealReminder(userId, mealType, message);
            return ResponseEntity.ok("提醒发送成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 发送营养提醒
    @PostMapping("/nutrition-reminder")
    public ResponseEntity<?> sendNutritionReminder(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String nutrient = (String) request.get("nutrient");
            String message = (String) request.get("message");
            messageService.sendNutritionReminder(userId, nutrient, message);
            return ResponseEntity.ok("提醒发送成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 发送目标进度提醒
    @PostMapping("/target-reminder")
    public ResponseEntity<?> sendTargetReminder(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String target = (String) request.get("target");
            String message = (String) request.get("message");
            messageService.sendTargetReminder(userId, target, message);
            return ResponseEntity.ok("提醒发送成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 手动触发目标进度提醒（测试用）
    @PostMapping("/trigger-target-progress")
    public ResponseEntity<?> triggerTargetProgress(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            // 注入 TargetProgressService
            org.springframework.context.ApplicationContext applicationContext = 
                org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(httpRequest.getServletContext());
            com.nutrition.service.TargetProgressService targetProgressService = 
                applicationContext.getBean(com.nutrition.service.TargetProgressService.class);
            
            targetProgressService.calculateAndSendTargetProgress(userId);
            return ResponseEntity.ok("目标进度提醒已触发");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("触发失败: " + e.getMessage());
        }
    }
}


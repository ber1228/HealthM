package com.nutrition.controller;

import com.nutrition.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AiRecommendController {

    @Autowired
    private ApiService apiService;


    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendation(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        try {
            // 构建提示词
            String prompt = buildPrompt(request);

            // 调用AI服务
            String aiResponse = apiService.query(prompt);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recommendation", aiResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "AI服务调用失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // 新增：通用AI对话接口
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatWithAI(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            // 获取用户消息
            String userMessage = (String) request.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("消息内容不能为空"));
            }

            // 获取可选的系统提示词
            String systemPrompt = (String) request.get("systemPrompt");

            // 调用AI对话服务
            String aiResponse = apiService.chat(userId, userMessage, systemPrompt);

            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("response", aiResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = createErrorResponse("AI对话失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // 新增：清除对话历史接口
    @PostMapping("/chat/clear")
    public ResponseEntity<Map<String, Object>> clearChatHistory(HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("用户未登录"));
            }

            apiService.clearConversation(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "对话历史已清除");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = createErrorResponse("清除对话历史失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // 辅助方法：创建错误响应
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorMessage);
        return errorResponse;
    }

    private String buildPrompt(Map<String, Object> request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的营养师，请根据以下用户信息提供个性化的饮食建议：\n\n");

        if (request.containsKey("age")) {
            prompt.append("年龄：").append(request.get("age")).append("岁\n");
        }
        if (request.containsKey("gender")) {
            prompt.append("性别：").append(request.get("gender")).append("\n");
        }
        if (request.containsKey("height")) {
            prompt.append("身高：").append(request.get("height")).append("cm\n");
        }
        if (request.containsKey("weight")) {
            prompt.append("体重：").append(request.get("weight")).append("kg\n");
        }
        if (request.containsKey("goal")) {
            prompt.append("目标：").append(request.get("goal")).append("\n");
        }
        if (request.containsKey("dietaryPreference")) {
            prompt.append("饮食偏好：").append(request.get("dietaryPreference")).append("\n");
        }

        prompt.append("请给出具体的饮食建议，包括三餐搭配和营养分析。");
        return prompt.toString();
    }
}
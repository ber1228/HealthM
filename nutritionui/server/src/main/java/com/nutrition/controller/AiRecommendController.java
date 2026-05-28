package com.nutrition.controller;

import com.nutrition.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiRecommendController {

    @Autowired
    private ApiService apiService;

    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendation(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            System.out.println("Received AI recommendation request: " + request);
            
            // 打印请求头信息用于调试
            System.out.println("Authorization header: " + httpRequest.getHeader("Authorization"));
            System.out.println("Content-Type header: " + httpRequest.getHeader("Content-Type"));
            
            String prompt = buildRecommendationPrompt(request);
            System.out.println("Generated prompt: " + prompt);
            
            String result = apiService.query(prompt);
            System.out.println("AI Service Result: " + result);
            
            // 空值检查
            if (result == null || result.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "AI服务返回空结果");
                errorResponse.put("data", "");
                System.out.println("Returning empty result error response");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 检查是否是错误消息
            if (result.contains("API密钥未配置") || 
                result.contains("未能获取到AI响应") || 
                result.contains("智能营养师现在不在线")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", result);
                errorResponse.put("data", "");
                System.out.println("Returning AI service error response: " + result);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 成功响应
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("data", result != null ? result : "");
            successResponse.put("message", "推荐生成成功");
            System.out.println("Returning success response with data length: " + 
                             (result != null ? result.length() : 0));
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "推荐生成失败：" + e.getMessage());
            errorResponse.put("data", "");
            System.out.println("Returning exception error response: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 添加无需认证的测试端点
    @GetMapping("/test-public")
    public ResponseEntity<Map<String, Object>> testApiServicePublic() {
        try {
            System.out.println("Testing AI service...");
            String testResult = apiService.query("你好，请简单介绍一下自己");
            System.out.println("Test result: " + testResult);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", testResult);
            response.put("message", "测试成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("data", "");
            response.put("message", "测试失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 简单测试端点，不调用AI服务
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", "");
        response.put("message", "AI服务健康检查正常");
        return ResponseEntity.ok(response);
    }
    
    // 添加测试端点
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testApiService() {
        try {
            String testResult = apiService.query("你好，请简单介绍一下自己");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", testResult);
            response.put("message", "测试成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("data", "");
            response.put("message", "测试失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private String buildRecommendationPrompt(Map<String, Object> request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下用户信息生成个性化的饮食推荐：\n");

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
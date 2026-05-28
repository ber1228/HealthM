package com.nutrition.controller;

import com.nutrition.dto.AuthResponse;
import com.nutrition.dto.LoginRequest;
import com.nutrition.dto.RegisterRequest;
import com.nutrition.dto.UserInfoRequest;
import com.nutrition.entity.User;
import com.nutrition.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = userService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        try {
            System.out.println("=== 获取用户信息 ===");
            System.out.println("请求头: " + request.getHeader("Authorization"));
            
            Long userId = (Long) request.getAttribute("userId");
            System.out.println("userId from attribute: " + userId);
            
            if (userId == null) {
                System.out.println("userId为null");
                return ResponseEntity.status(401).body("未登录");
            }
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(404).body("用户不存在");
            }
            // 不返回密码字段
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("获取用户信息失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/user")
    public ResponseEntity<?> updateUserInfo(@RequestBody UserInfoRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            userService.updateUserInfo(userId, request);
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


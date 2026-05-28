package com.nutrition.controller;

import com.nutrition.dto.AuthResponse;
import com.nutrition.dto.LoginRequest;
import com.nutrition.dto.RegisterRequest;
import com.nutrition.dto.UserInfoRequest;
import com.nutrition.entity.User;
import com.nutrition.service.UserService;
import com.nutrition.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 获取用户信息
            User user = userService.findByUsername(loginRequest.getUsername());
            
            // 简单验证密码（实际项目中应该使用更安全的方式）
            if (user == null || !userService.checkPassword(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null, "用户名或密码错误"));
            }
            
            // 生成新的JWT令牌
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse(null, "登录过程中发生错误"));
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
package com.nutrition.controller;

import com.nutrition.entity.User;
import com.nutrition.mapper.UserMapper;
import com.nutrition.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private AuthUtil authUtil;
    
    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            List<User> users = userMapper.findAll();
            // 不返回密码字段
            users.forEach(u -> u.setPassword(null));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取用户列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            User user = userMapper.findByIdWithRole(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取用户失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        if (!authUtil.isAdmin(httpRequest)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            String role = request.get("role");
            if (!"ADMIN".equals(role) && !"USER".equals(role)) {
                return ResponseEntity.badRequest().body("无效的角色值");
            }
            int rows = userMapper.updateRole(id, role);
            if (rows > 0) {
                return ResponseEntity.ok("角色更新成功");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新角色失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            // 不能删除自己
            Long currentUserId = (Long) request.getAttribute("userId");
            if (currentUserId != null && currentUserId.equals(id)) {
                return ResponseEntity.badRequest().body("不能删除自己的账号");
            }
            int rows = userMapper.deleteById(id);
            if (rows > 0) {
                return ResponseEntity.ok("删除成功");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }
            User user = userMapper.findByIdWithRole(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            user.setPassword(null);
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("isAdmin", "ADMIN".equals(user.getRole()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取用户信息失败: " + e.getMessage());
        }
    }
}


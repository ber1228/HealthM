package com.nutrition.util;

import com.nutrition.entity.User;
import com.nutrition.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthUtil {
    
    @Autowired
    private UserMapper userMapper;
    
    public boolean isAdmin(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) return false;
            User user = userMapper.findByIdWithRole(userId);
            return user != null && "ADMIN".equals(user.getRole());
        } catch (Exception e) {
            return false;
        }
    }
    
    public User getCurrentUser(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) return null;
            return userMapper.findByIdWithRole(userId);
        } catch (Exception e) {
            return null;
        }
    }
}


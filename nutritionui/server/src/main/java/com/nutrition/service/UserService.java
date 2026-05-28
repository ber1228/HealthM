package com.nutrition.service;

import com.nutrition.dto.AuthResponse;
import com.nutrition.dto.LoginRequest;
import com.nutrition.dto.RegisterRequest;
import com.nutrition.dto.UserInfoRequest;
import com.nutrition.entity.User;
import com.nutrition.mapper.UserMapper;
import com.nutrition.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(md5Hash(request.getPassword()));
        user.setRole("USER"); // 默认普通用户
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        
        userMapper.insert(user);
        
        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }
    
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return md5Hash(rawPassword).equals(encodedPassword);
    }
    
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    public User getUserById(Long userId) {
        return userMapper.findById(userId);
    }
    
    public void updateUserInfo(Long userId, UserInfoRequest request) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查健康目标是否改变
        String oldHealthGoal = user.getHealthGoal();
        String newHealthGoal = request.getHealthGoal();
        boolean goalChanged = oldHealthGoal == null || !oldHealthGoal.equals(newHealthGoal);
        
        // 如果目标改变且新目标不为空，记录目标开始日期
        if (goalChanged && newHealthGoal != null && !newHealthGoal.isEmpty() && !"维持健康".equals(newHealthGoal)) {
            // 判断是否需要更新目标开始日期
            if (oldHealthGoal == null || oldHealthGoal.isEmpty() || "维持健康".equals(oldHealthGoal)) {
                // 这是首次设置目标或从维持健康切换到具体目标
                user.setGoalStartDate(new java.sql.Timestamp(System.currentTimeMillis()));
                user.setInitialWeight(request.getWeight()); // 记录初始体重
            }
        }
        
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setActivityLevel(request.getActivityLevel());
        user.setHealthGoal(request.getHealthGoal());
        
        // 保存目标值
        if (request.getTargetValue() != null) {
            user.setTargetValue(request.getTargetValue());
        }
        
        // 计算基础代谢率和每日推荐热量
        calculateNutritionTargets(user);
        
        userMapper.update(user);
    }
    
    private void calculateNutritionTargets(User user) {
        // 计算BMR（基础代谢率）- 使用Harris-Benedict公式
        BigDecimal bmr;
        if (user.getGender() == 1) { // 男性
            bmr = new BigDecimal("88.362").add(new BigDecimal("13.397").multiply(user.getWeight()))
                    .add(new BigDecimal("4.799").multiply(user.getHeight()))
                    .subtract(new BigDecimal("5.677").multiply(new BigDecimal(user.getAge())));
        } else { // 女性
            bmr = new BigDecimal("447.593").add(new BigDecimal("9.247").multiply(user.getWeight()))
                    .add(new BigDecimal("3.098").multiply(user.getHeight()))
                    .subtract(new BigDecimal("4.330").multiply(new BigDecimal(user.getAge())));
        }
        
        // 根据活动量调整
        BigDecimal activityFactor = BigDecimal.ONE;
        switch (user.getActivityLevel()) {
            case 1: activityFactor = new BigDecimal("1.2"); break; // 久坐
            case 2: activityFactor = new BigDecimal("1.375"); break; // 轻度
            case 3: activityFactor = new BigDecimal("1.55"); break; // 中度
            case 4: activityFactor = new BigDecimal("1.725"); break; // 高强度
        }
        
        BigDecimal dailyCalories = bmr.multiply(activityFactor);
        
        // 根据健康目标调整
        if ("减重".equals(user.getHealthGoal())) {
            dailyCalories = dailyCalories.multiply(new BigDecimal("0.8")); // 减少20%
        } else if ("增肌".equals(user.getHealthGoal())) {
            dailyCalories = dailyCalories.multiply(new BigDecimal("1.2")); // 增加20%
        }
        
        user.setBmr(bmr);
        user.setDailyCalories(dailyCalories);
    }
    
    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}
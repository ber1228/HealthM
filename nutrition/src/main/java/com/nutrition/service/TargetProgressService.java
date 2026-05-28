package com.nutrition.service;

import com.nutrition.entity.NutritionAnalysis;
import com.nutrition.entity.User;
import com.nutrition.mapper.NutritionAnalysisMapper;
import com.nutrition.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TargetProgressService {
    
    @Autowired
    private NutritionAnalysisMapper nutritionAnalysisMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private MessageService messageService;
    
    /**
     * 计算目标进度并发送提醒
     */
    public void calculateAndSendTargetProgress(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            System.out.println("TargetProgressService - 用户不存在, userId: " + userId);
            return;
        }
        
        if (user.getHealthGoal() == null || user.getHealthGoal().isEmpty() || "维持健康".equals(user.getHealthGoal())) {
            System.out.println("TargetProgressService - 用户没有设置健康目标, userId: " + userId);
            return;
        }
        
        String healthGoal = user.getHealthGoal();
        System.out.println("TargetProgressService - 计算进度, 用户: " + user.getUsername() + ", 目标: " + healthGoal);
        
        String progress = "";
        String suggestion = "";
        
        switch (healthGoal) {
            case "减重":
                Map<String, Object> weightLossProgress = calculateWeightLossProgress(userId);
                progress = (String) weightLossProgress.get("progress");
                suggestion = (String) weightLossProgress.get("suggestion");
                System.out.println("TargetProgressService - 减重进度: " + progress);
                break;
            case "增肌":
                Map<String, Object> muscleGainProgress = calculateMuscleGainProgress(userId);
                progress = (String) muscleGainProgress.get("progress");
                suggestion = (String) muscleGainProgress.get("suggestion");
                System.out.println("TargetProgressService - 增肌进度: " + progress);
                break;
            case "控制血糖":
                Map<String, Object> bloodSugarProgress = calculateBloodSugarProgress(userId);
                progress = (String) bloodSugarProgress.get("progress");
                suggestion = (String) bloodSugarProgress.get("suggestion");
                System.out.println("TargetProgressService - 血糖控制进度: " + progress);
                break;
            case "控制血压":
                Map<String, Object> bloodPressureProgress = calculateBloodPressureProgress(userId);
                progress = (String) bloodPressureProgress.get("progress");
                suggestion = (String) bloodPressureProgress.get("suggestion");
                System.out.println("TargetProgressService - 血压控制进度: " + progress);
                break;
            default:
                System.out.println("TargetProgressService - 未识别的健康目标: " + healthGoal);
                return;
        }
        
        if (progress != null && !progress.isEmpty()) {
            System.out.println("TargetProgressService - 发送提醒, 进度: " + progress);
            messageService.sendAutoTargetReminder(userId, healthGoal, progress, suggestion);
            System.out.println("TargetProgressService - 提醒已发送");
        } else {
            System.out.println("TargetProgressService - 进度为空，不发送提醒");
        }
    }
    
    /**
     * 计算减重进度
     */
    private Map<String, Object> calculateWeightLossProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userMapper.findById(userId);
        if (user == null || user.getGoalStartDate() == null) {
            result.put("progress", "您已开始减重饮食计划，请继续保持！");
            result.put("suggestion", "建议您保持当前的饮食节奏，适当增加运动量，有助于提高减重效果。");
            return result;
        }
        
        // 从目标开始日期计算天数
        long startTime = user.getGoalStartDate().getTime();
        long currentTime = System.currentTimeMillis();
        int daysElapsed = (int) ((currentTime - startTime) / (24 * 60 * 60 * 1000));
        
        if (daysElapsed < 1) {
            result.put("progress", "您刚开始减重饮食计划，请继续保持！");
            result.put("suggestion", "建议您保持当前的饮食节奏，适当增加运动量，有助于提高减重效果。");
            return result;
        }
        
        // 获取初始体重和当前体重
        BigDecimal initialWeight = user.getInitialWeight();
        BigDecimal currentWeight = user.getWeight();
        
        if (initialWeight == null || currentWeight == null) {
            result.put("progress", "您已坚持减重饮食计划 " + daysElapsed + " 天，请继续保持！");
            result.put("suggestion", "建议您保持当前的饮食节奏，适当增加运动量，有助于提高减重效果。");
            return result;
        }
        
        // 计算减重量
        BigDecimal weightLoss = initialWeight.subtract(currentWeight);
        
        // 计算周数
        int weeks = daysElapsed / 7;
        
        // 获取目标值
        BigDecimal targetValue = user.getTargetValue();
        String remainingGoal = "";
        if (targetValue != null && weightLoss.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remaining = targetValue.subtract(weightLoss);
            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                remainingGoal = String.format("，距离目标还有 %.1f kg", remaining.doubleValue());
            }
        }
        
        String progress = String.format("您已坚持减重饮食计划 %d 周（%d 天），累计减重 %.1f kg%s", 
                weeks, daysElapsed, weightLoss.doubleValue(), remainingGoal);
        
        String suggestion;
        if (weightLoss.compareTo(new BigDecimal("1")) > 0) {
            suggestion = "进展不错！请继续坚持当前的饮食节奏，适当增加运动量，有助于提高减重效果。";
        } else if (weightLoss.compareTo(BigDecimal.ZERO) > 0) {
            suggestion = "您的减重计划正在稳步进行中，建议保持当前的饮食节奏，可以考虑适当增加运动量。";
        } else {
            suggestion = "建议您适当减少热量摄入，增加蛋白质和膳食纤维摄入，配合适量有氧运动。";
        }
        
        result.put("progress", progress);
        result.put("suggestion", suggestion);
        
        return result;
    }
    
    /**
     * 计算增肌进度
     */
    private Map<String, Object> calculateMuscleGainProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userMapper.findById(userId);
        if (user == null || user.getGoalStartDate() == null) {
            result.put("progress", "您已开始增肌饮食计划，请继续保持！");
            result.put("suggestion", "建议继续增加蛋白质摄入，保证充足的热量和钙质，配合力量训练。");
            return result;
        }
        
        // 从目标开始日期计算天数
        long startTime = user.getGoalStartDate().getTime();
        long currentTime = System.currentTimeMillis();
        int daysElapsed = (int) ((currentTime - startTime) / (24 * 60 * 60 * 1000));
        
        if (daysElapsed < 1) {
            result.put("progress", "您刚开始增肌饮食计划，请继续保持！");
            result.put("suggestion", "建议继续增加蛋白质摄入，保证充足的热量和钙质，配合力量训练。");
            return result;
        }
        
        // 获取近期营养分析（最多30天）
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = new Date(calendar.getTimeInMillis());
        
        List<NutritionAnalysis> analyses = nutritionAnalysisMapper.findByUserAndDateRange(userId, startDate, endDate);
        
        int dayCount = 0;
        int highProteinDays = 0;
        
        for (NutritionAnalysis analysis : analyses) {
            if (analysis.getTotalProtein() != null && analysis.getTargetProtein() != null) {
                dayCount++;
                // 检查蛋白质摄入是否充足（超过目标的80%）
                if (analysis.getTotalProtein().compareTo(analysis.getTargetProtein().multiply(new BigDecimal("0.8"))) >= 0) {
                    highProteinDays++;
                }
            }
        }
        
        int weeks = daysElapsed / 7;
        String progress = String.format("您已坚持增肌饮食计划 %d 周（%d 天），其中 %d 天蛋白质摄入充足", 
                weeks, daysElapsed, highProteinDays);
        
        String suggestion;
        if (dayCount > 0 && highProteinDays >= dayCount * 0.8) {
            suggestion = "您的增肌计划执行得很好！建议继续增加蛋白质摄入，保证充足的热量和钙质，配合力量训练。";
        } else if (dayCount > 0 && highProteinDays >= dayCount * 0.5) {
            suggestion = "请继续保持，建议增加更多蛋白质含量高的食物，配合力量训练。";
        } else {
            suggestion = "建议增加蛋白质摄入，多吃瘦肉、鸡蛋、豆制品等，配合力量训练。";
        }
        
        result.put("progress", progress);
        result.put("suggestion", suggestion);
        
        return result;
    }
    
    /**
     * 计算血糖控制进度
     */
    private Map<String, Object> calculateBloodSugarProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userMapper.findById(userId);
        if (user == null || user.getGoalStartDate() == null) {
            result.put("progress", "您已开始血糖控制计划，请继续保持！");
            result.put("suggestion", "建议继续选择低升糖指数食材，增加膳食纤维摄入。");
            return result;
        }
        
        // 从目标开始日期计算天数
        long startTime = user.getGoalStartDate().getTime();
        long currentTime = System.currentTimeMillis();
        int daysElapsed = (int) ((currentTime - startTime) / (24 * 60 * 60 * 1000));
        
        if (daysElapsed < 1) {
            result.put("progress", "您刚开始血糖控制计划，请继续保持！");
            result.put("suggestion", "建议继续选择低升糖指数食材，增加膳食纤维摄入。");
            return result;
        }
        
        // 获取近期营养分析（最多30天）
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = new Date(calendar.getTimeInMillis());
        
        List<NutritionAnalysis> analyses = nutritionAnalysisMapper.findByUserAndDateRange(userId, startDate, endDate);
        
        int dayCount = 0;
        int goodDays = 0;
        
        for (NutritionAnalysis analysis : analyses) {
            if (analysis.getTotalCarbs() != null && analysis.getTargetCarbs() != null) {
                dayCount++;
                // 检查碳水化合物摄入是否在目标范围内
                if (analysis.getTotalCarbs().compareTo(analysis.getTargetCarbs().multiply(new BigDecimal("1.1"))) <= 0) {
                    goodDays++;
                }
            }
        }
        
        int weeks = daysElapsed / 7;
        String progress = String.format("您已坚持血糖控制计划 %d 周（%d 天），其中 %d 天血糖控制良好", 
                weeks, daysElapsed, goodDays);
        
        String suggestion;
        if (dayCount > 0 && goodDays >= dayCount * 0.8) {
            suggestion = "您的血糖控制做得很好！建议继续选择低升糖指数食材，增加膳食纤维摄入。";
        } else {
            suggestion = "建议减少高糖高淀粉食物摄入，选择低升糖指数食材，如燕麦、糙米、绿叶蔬菜。";
        }
        
        result.put("progress", progress);
        result.put("suggestion", suggestion);
        
        return result;
    }
    
    /**
     * 计算血压控制进度
     */
    private Map<String, Object> calculateBloodPressureProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userMapper.findById(userId);
        if (user == null || user.getGoalStartDate() == null) {
            result.put("progress", "您已开始血压控制计划，请继续保持！");
            result.put("suggestion", "建议继续减少钠摄入，增加钾和膳食纤维摄入。");
            return result;
        }
        
        // 从目标开始日期计算天数
        long startTime = user.getGoalStartDate().getTime();
        long currentTime = System.currentTimeMillis();
        int daysElapsed = (int) ((currentTime - startTime) / (24 * 60 * 60 * 1000));
        
        if (daysElapsed < 1) {
            result.put("progress", "您刚开始血压控制计划，请继续保持！");
            result.put("suggestion", "建议继续减少钠摄入，增加钾和膳食纤维摄入。");
            return result;
        }
        
        // 获取近期营养分析（最多30天）
        Calendar calendar = Calendar.getInstance();
        Date endDate = new Date(System.currentTimeMillis());
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date startDate = new Date(calendar.getTimeInMillis());
        
        List<NutritionAnalysis> analyses = nutritionAnalysisMapper.findByUserAndDateRange(userId, startDate, endDate);
        
        int dayCount = 0;
        int goodDays = 0;
        
        for (NutritionAnalysis analysis : analyses) {
            if (analysis.getTotalSodium() != null && analysis.getTotalCalories() != null) {
                dayCount++;
                // 检查钠摄入是否合理（简化判断：钠摄入不超过2300mg）
                if (analysis.getTotalSodium().compareTo(new BigDecimal("2300")) <= 0) {
                    goodDays++;
                }
            }
        }
        
        int weeks = daysElapsed / 7;
        String progress = String.format("您已坚持血压控制计划 %d 周（%d 天），其中 %d 天血压控制良好", 
                weeks, daysElapsed, goodDays);
        
        String suggestion;
        if (dayCount > 0 && goodDays >= dayCount * 0.8) {
            suggestion = "您的血压控制做得很好！建议继续减少钠摄入，增加钾和膳食纤维摄入。";
        } else {
            suggestion = "建议减少盐、酱油等调味品的使用，少吃腌制食品和加工肉类，增加蔬菜水果摄入。";
        }
        
        result.put("progress", progress);
        result.put("suggestion", suggestion);
        
        return result;
    }
}


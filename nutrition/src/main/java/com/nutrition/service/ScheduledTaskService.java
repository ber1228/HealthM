package com.nutrition.service;

import com.nutrition.entity.DailyPlan;
import com.nutrition.entity.Reminder;
import com.nutrition.entity.User;
import com.nutrition.mapper.DailyPlanMapper;
import com.nutrition.mapper.ReminderMapper;
import com.nutrition.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;

@Service
public class ScheduledTaskService {
    
    @Autowired
    private ReminderMapper reminderMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private DailyPlanMapper dailyPlanMapper;
    
    @Autowired
    private TargetProgressService targetProgressService;
    
    // 每5分钟执行一次，检查是否需要发送提醒（饮食提醒、营养提醒、目标进度提醒）
    @Scheduled(cron = "0 */1 * * * ?")
    public void checkAndSendAllReminders() {
        System.out.println("========== 开始检查所有提醒 ==========");
        
        // 获取当前日期和时间
        Date today = new Date(System.currentTimeMillis());
        LocalTime now = LocalTime.now();
        
        // 获取所有启用的提醒
        List<Reminder> reminders = reminderMapper.findAllEnabled();
        System.out.println("找到启用的提醒数: " + reminders.size());
        
        for (Reminder reminder : reminders) {
            System.out.println("检查提醒: userId=" + reminder.getUserId() + ", type=" + reminder.getReminderType() + ", time=" + reminder.getReminderTime());
            
            // 检查时间是否匹配（允许5分钟误差）
            if (isTimeMatch(reminder.getReminderTime(), now)) {
                System.out.println("时间匹配! 检查星期...");
                // 检查星期是否匹配
                if (isDayMatch(reminder.getDaysOfWeek())) {
                    System.out.println("星期匹配! 开始发送提醒...");
                    
                    String reminderType = reminder.getReminderType();
                    
                    if ("meal".equals(reminderType)) {
                        // 发送饮食提醒
                        sendMealReminder(reminder.getUserId(), today);
                    } else if ("nutrition".equals(reminderType)) {
                        // 发送营养提醒
                        sendNutritionReminder(reminder.getUserId());
                    } else if ("target".equals(reminderType)) {
                        // 发送目标进度提醒
                        User user = userMapper.findById(reminder.getUserId());
                        if (user != null && user.getHealthGoal() != null && !user.getHealthGoal().isEmpty() && !"维持健康".equals(user.getHealthGoal())) {
                            System.out.println("发送目标进度提醒给用户: " + user.getUsername());
                            calculateAndSendTargetProgress(user);
                        } else {
                            System.out.println("用户没有设置健康目标，跳过目标进度提醒");
                        }
                    }
                    System.out.println("提醒已发送完成");
                } else {
                    System.out.println("星期不匹配");
                }
            } else {
                System.out.println("时间不匹配");
            }
        }
        
        System.out.println("========== 提醒检查完成 ==========");
    }
    
    // 发送营养提醒（基于最近的营养分析数据）
    private void sendNutritionReminder(Long userId) {
        try {
            // 发送通用营养提醒
            String message = "温馨提醒：建议您关注近期的营养摄入情况，保持均衡饮食。如有营养素不足或超标，系统会及时提醒您。";
            
            messageService.sendNutritionReminder(userId, "营养状况", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 发送饮食提醒
    private void sendMealReminder(Long userId, Date date) {
        try {
            User user = userMapper.findById(userId);
            if (user == null) {
                return;
            }
            
            // 获取今日饮食方案
            List<DailyPlan> plans = dailyPlanMapper.findByUserAndDate(userId, date);
            
            if (plans.isEmpty()) {
                messageService.sendMealReminder(userId, "今日", "您今天还没有设置饮食方案，请前往饮食方案页面生成今日饮食计划。");
                return;
            }
            
            // 根据当前时间确定餐次
            String mealType = getCurrentMealType();
            String mealPlans = getMealPlansContent(plans, mealType);
            
            String message = String.format("温馨提醒：现在是%s时间了！\n今日%s方案：\n%s\n请按时进食，保持健康的饮食节奏。",
                    getMealTypeName(mealType), mealType, mealPlans);
            
            messageService.sendMealReminder(userId, mealType, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 计算并发送目标进度提醒
    private void calculateAndSendTargetProgress(User user) {
        try {
            targetProgressService.calculateAndSendTargetProgress(user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 检查时间是否匹配
    private boolean isTimeMatch(java.sql.Time reminderTime, LocalTime now) {
        if (reminderTime == null) {
            return false;
        }
        
        LocalTime reminderLocalTime = reminderTime.toLocalTime();
        int reminderHour = reminderLocalTime.getHour();
        int reminderMinute = reminderLocalTime.getMinute();
        int nowHour = now.getHour();
        int nowMinute = now.getMinute();
        
        // 允许5分钟误差
        return reminderHour == nowHour && Math.abs(reminderMinute - nowMinute) <= 5;
    }
    
    // 检查星期是否匹配
    private boolean isDayMatch(String daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return true; // 如果没有设置，默认每天
        }
        
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // 转换为1-7格式（周一到周日）
        int day = (dayOfWeek == 1) ? 7 : dayOfWeek - 1;
        
        return daysOfWeek.contains(String.valueOf(day));
    }
    
    // 获取当前餐次
    private String getCurrentMealType() {
        int hour = LocalTime.now().getHour();
        
        if (hour >= 6 && hour < 9) {
            return "breakfast";
        } else if (hour >= 9 && hour < 11) {
            return "morning_snack";
        } else if (hour >= 11 && hour < 14) {
            return "lunch";
        } else if (hour >= 14 && hour < 17) {
            return "afternoon_snack";
        } else if (hour >= 17 && hour < 20) {
            return "dinner";
        } else {
            return "evening_snack";
        }
    }
    
    // 获取餐次名称
    private String getMealTypeName(String mealType) {
        switch (mealType) {
            case "breakfast": return "早餐";
            case "morning_snack": return "上午加餐";
            case "lunch": return "午餐";
            case "afternoon_snack": return "下午加餐";
            case "dinner": return "晚餐";
            case "evening_snack": return "晚上加餐";
            default: return mealType;
        }
    }
    
    // 获取指定餐次的内容
    private String getMealPlansContent(List<DailyPlan> plans, String mealType) {
        StringBuilder content = new StringBuilder();
        for (DailyPlan plan : plans) {
            if (mealType.equals(plan.getMealType())) {
                content.append("- ").append(plan.getFoodName())
                      .append(" ").append(plan.getAmount()).append("g\n");
            }
        }
        return content.length() > 0 ? content.toString() : "暂无安排";
    }
}


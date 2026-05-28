package com.nutrition.controller;

import com.nutrition.entity.MealRecord;
import com.nutrition.entity.NutritionAnalysis;
import com.nutrition.mapper.MealRecordMapper;
import com.nutrition.mapper.FoodMapper;
import com.nutrition.entity.Food;
import com.nutrition.service.MealRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/record")
@CrossOrigin
public class RecordController {
    
    @Autowired
    private MealRecordMapper mealRecordMapper;
    
    @Autowired
    private FoodMapper foodMapper;
    
    @Autowired
    private MealRecordService mealRecordService;
    
    @GetMapping("/{date}")
    public ResponseEntity<?> getRecords(@PathVariable String date, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date recordDate = Date.valueOf(date);
            List<MealRecord> records = mealRecordMapper.findByUserAndDate(userId, recordDate);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping
    public ResponseEntity<?> addRecord(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            MealRecord record = new MealRecord();
            record.setUserId(userId);
            
            if (requestData.get("date") != null) {
                String dateStr = requestData.get("date").toString();
                record.setDate(Date.valueOf(dateStr));
            } else {
                return ResponseEntity.badRequest().body("日期不能为空");
            }
            
            if (requestData.get("mealType") != null) {
                record.setMealType(requestData.get("mealType").toString());
            } else {
                return ResponseEntity.badRequest().body("餐次不能为空");
            }
            
            if (requestData.get("foodId") != null) {
                record.setFoodId(Long.valueOf(requestData.get("foodId").toString()));
            } else {
                return ResponseEntity.badRequest().body("请选择食材");
            }
            
            if (requestData.get("foodName") != null) {
                record.setFoodName(requestData.get("foodName").toString());
            }
            
            if (requestData.get("amount") != null) {
                record.setAmount(new java.math.BigDecimal(requestData.get("amount").toString()));
            } else {
                return ResponseEntity.badRequest().body("数量不能为空");
            }
            
            if (record.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("数量必须大于0");
            }
            
            Food food = foodMapper.findById(record.getFoodId());
            if (food != null) {
                java.math.BigDecimal amount = record.getAmount();
                java.math.BigDecimal divisor = java.math.BigDecimal.valueOf(100);
                
                if (food.getCalories() != null) {
                    record.setCalories(food.getCalories().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getProtein() != null) {
                    record.setProtein(food.getProtein().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getCarbs() != null) {
                    record.setCarbs(food.getCarbs().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getFat() != null) {
                    record.setFat(food.getFat().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getFiber() != null) {
                    record.setFiber(food.getFiber().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getCalcium() != null) {
                    record.setCalcium(food.getCalcium().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getIron() != null) {
                    record.setIron(food.getIron().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getVitc() != null) {
                    record.setVitc(food.getVitc().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getPotassium() != null) {
                    record.setPotassium(food.getPotassium().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                if (food.getSodium() != null) {
                    record.setSodium(food.getSodium().multiply(amount).divide(divisor, 2, java.math.RoundingMode.HALF_UP));
                }
                
                if (record.getFoodName() == null || record.getFoodName().isEmpty()) {
                    record.setFoodName(food.getName());
                }
            } else {
                return ResponseEntity.badRequest().body("未找到食材，请重新选择");
            }
            
            mealRecordMapper.insert(record);
            return ResponseEntity.ok("添加成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecord(@PathVariable Long id) {
        try {
            mealRecordMapper.delete(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // 分析每日饮食记录
    @PostMapping("/analyze/{date}")
    public ResponseEntity<?> analyzeDailyRecords(@PathVariable String date, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date recordDate = Date.valueOf(date);
            Map<String, Object> analysis = mealRecordService.analyzeDailyRecords(userId, recordDate);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 分析饮食方案
    @PostMapping("/analyze-plan/{date}")
    public ResponseEntity<?> analyzeDailyPlan(@PathVariable String date, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date planDate = Date.valueOf(date);
            Map<String, Object> analysis = mealRecordService.analyzeDailyPlan(userId, planDate);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 获取营养分析历史
    @GetMapping("/nutrition-history")
    public ResponseEntity<?> getNutritionHistory(@RequestParam String startDate, @RequestParam String endDate, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date start = Date.valueOf(startDate);
            Date end = Date.valueOf(endDate);
            List<NutritionAnalysis> history = mealRecordService.getNutritionHistory(userId, start, end);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 获取统计数据
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(@RequestParam String startDate, @RequestParam String endDate, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date start = Date.valueOf(startDate);
            Date end = Date.valueOf(endDate);
            List<Map<String, Object>> statistics = mealRecordService.getStatistics(userId, start, end);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 按周生成营养分析报告
    @PostMapping("/analyze-weekly/{weekStart}")
    public ResponseEntity<?> analyzeWeekly(@PathVariable String weekStart, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date weekStartDate = Date.valueOf(weekStart);
            Map<String, Object> report = mealRecordService.generateWeeklyReport(userId, weekStartDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // 按月生成营养分析报告
    @PostMapping("/analyze-monthly/{year}/{month}")
    public ResponseEntity<?> analyzeMonthly(@PathVariable int year, @PathVariable int month, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> report = mealRecordService.generateMonthlyReport(userId, year, month);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}


package com.nutrition.controller;

import com.nutrition.entity.DailyPlan;
import com.nutrition.service.PlanService;
import com.nutrition.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/daily-plan")
public class DailyPlanController {

    @Autowired
    private PlanService planService;

    @Autowired
    private AuthUtil authUtil;

    // 1. 生成今日饮食方案
    @PostMapping("/generate/today")
    public ResponseEntity<Map<String, Object>> generateTodayPlan(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Date today = new Date(System.currentTimeMillis());

        List<DailyPlan> plans = planService.generateDailyPlan(userId, today);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "生成成功");
        response.put("data", plans);
        return ResponseEntity.ok(response);
    }

    // 2. 生成指定日期饮食方案
    @PostMapping("/generate/{date}")
    public ResponseEntity<Map<String, Object>> generateDatePlan(
            HttpServletRequest request,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        Long userId = (Long) request.getAttribute("userId");

        List<DailyPlan> plans = planService.generateDailyPlan(userId, date);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "生成成功");
        response.put("data", plans);
        return ResponseEntity.ok(response);
    }

    // 3. 获取指定日期饮食方案
    @GetMapping("/{date}")
    public ResponseEntity<Map<String, Object>> getDailyPlan(
            HttpServletRequest request,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        Long userId = (Long) request.getAttribute("userId");

        List<DailyPlan> plans = planService.getDailyPlan(userId, date);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", plans);
        return ResponseEntity.ok(response);
    }

    // 4. 替换食材
    @PostMapping("/replace")
    public ResponseEntity<Map<String, Object>> replaceFood(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");

        Long planId = Long.valueOf(params.get("planId").toString());
        Long newFoodId = Long.valueOf(params.get("newFoodId").toString());
        BigDecimal newAmount = new BigDecimal(params.get("newAmount").toString());
        String date = params.get("date").toString();

        Map<String, Object> result = planService.replaceFood(userId, planId, newFoodId, newAmount, date);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "替换成功");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    // 5. 获取相似食材推荐
    @GetMapping("/similar/{foodId}")
    public ResponseEntity<Map<String, Object>> getSimilarFoods(
            HttpServletRequest request,
            @PathVariable Long foodId,
            @RequestParam(defaultValue = "5") int limit) {
        Long userId = (Long) request.getAttribute("userId");

        List<Map<String, Object>> similarFoods = planService.getSimilarFoods(foodId, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", similarFoods);
        return ResponseEntity.ok(response);
    }

    // 6. 重新生成方案（根据用户需求）
    @PostMapping("/regenerate")
    public ResponseEntity<Map<String, Object>> regeneratePlan(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");

        Date date = Date.valueOf(params.get("date").toString());
        String requirements = params.get("requirements").toString();

        List<DailyPlan> plans = planService.regeneratePlan(userId, date, requirements);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "重新生成成功");
        response.put("data", plans);
        return ResponseEntity.ok(response);
    }

    // 8. 获取DRI符合性检查结果
    @PostMapping("/check-dri")
    public ResponseEntity<Map<String, Object>> checkDRICompliance(
            HttpServletRequest request,
            @RequestBody Map<String, Object> nutritionChange) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> driCheck = planService.checkDRICompliance(userId, nutritionChange);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "检查完成");
        response.put("data", driCheck);
        return ResponseEntity.ok(response);
    }
}
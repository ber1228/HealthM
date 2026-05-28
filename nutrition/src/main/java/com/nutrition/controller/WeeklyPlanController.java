package com.nutrition.controller;

import com.nutrition.entity.WeeklyPlan;
import com.nutrition.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weekly-plan")
public class WeeklyPlanController {

    @Autowired
    private PlanService planService;

    // 1. 生成周方案
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateWeeklyPlan(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date weekStart) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = planService.generateWeeklyPlan(userId, weekStart);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "周方案生成成功");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    // 2. 获取周方案
    @GetMapping("/{weekStart}")
    public ResponseEntity<Map<String, Object>> getWeeklyPlan(
            HttpServletRequest request,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date weekStart) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = planService.getWeeklyPlan(userId, weekStart);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    // 3. 保存为模板
    @PostMapping("/save-template")
    public ResponseEntity<Map<String, Object>> saveWeeklyTemplate(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");

        Date weekStart = Date.valueOf(params.get("weekStart").toString());
        String templateName = params.get("templateName").toString();

        planService.saveWeeklyTemplate(userId, weekStart, templateName);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "模板保存成功");
        return ResponseEntity.ok(response);
    }

    // 4. 应用模板
    @PostMapping("/apply-template")
    public ResponseEntity<Map<String, Object>> applyTemplate(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");

        Long templateId = Long.valueOf(params.get("templateId").toString());
        Date weekStart = Date.valueOf(params.get("weekStart").toString());

        Map<String, Object> result = planService.applyTemplate(userId, templateId, weekStart);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "模板应用成功");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    // 5. 获取用户模板列表
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getUserTemplates(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        List<WeeklyPlan> templates = planService.getUserTemplates(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", templates);
        return ResponseEntity.ok(response);
    }

    // 6. 获取模板详情
    @GetMapping("/template/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplateDetail(
            HttpServletRequest request,
            @PathVariable Long templateId) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = planService.getTemplateDetail(userId, templateId);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    // 7. 更新模板食材
    @PostMapping("/template/update-food")
    public ResponseEntity<Map<String, Object>> updateTemplateFood(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");

        Long planId = Long.valueOf(params.get("planId").toString());
        Long newFoodId = Long.valueOf(params.get("newFoodId").toString());
        Long templateId = Long.valueOf(params.get("templateId").toString());
        String date = params.get("date").toString();
        Double amount = Double.valueOf(params.get("amount").toString());

        Map<String, Object> result = planService.updateTemplateFood(userId, planId, newFoodId,
                new java.math.BigDecimal(amount.toString()), templateId, date);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "更新成功");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    // 8. 删除模板
    @DeleteMapping("/template/{templateId}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(
            HttpServletRequest request,
            @PathVariable Long templateId) {
        Long userId = (Long) request.getAttribute("userId");

        planService.deleteTemplate(userId, templateId);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }

    // 9. 应用周方案到每日
    @PostMapping("/apply-to-daily")
    public ResponseEntity<Map<String, Object>> applyWeeklyPlanToDaily(
            HttpServletRequest request,
            @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");

        Date weekStart = Date.valueOf(params.get("weekStart").toString());

        Map<String, Object> result = planService.applyWeeklyPlan(userId, weekStart);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "应用成功");
        response.put("data", result);
        return ResponseEntity.ok(response);
    }
}
package com.nutrition.controller;

import com.nutrition.entity.DailyPlan;
import com.nutrition.entity.WeeklyPlan;
import com.nutrition.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plan")
@CrossOrigin
public class PlanController {
    
    @Autowired
    private PlanService planService;
    
    @PostMapping("/generate/{date}")
    public ResponseEntity<?> generateDailyPlan(@PathVariable String date, HttpServletRequest request) {
        try {
            System.out.println("=== 生成饮食方案 ===");
            System.out.println("日期: " + date);
            Long userId = (Long) request.getAttribute("userId");
            System.out.println("userId: " + userId);
            
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录");
            }
            
            Date planDate = Date.valueOf(date);
            List<DailyPlan> plans = planService.generateDailyPlan(userId, planDate);
            System.out.println("生成成功，方案数量: " + plans.size());
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            System.out.println("生成方案失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/{date}")
    public ResponseEntity<?> getDailyPlan(@PathVariable String date, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date planDate = Date.valueOf(date);
            List<DailyPlan> plans = planService.getDailyPlan(userId, planDate);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/weekly/generate/{weekStart}")
    public ResponseEntity<?> generateWeeklyPlan(@PathVariable String weekStart, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date weekStartDate = Date.valueOf(weekStart);
            Map<String, Object> result = planService.generateWeeklyPlan(userId, weekStartDate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/weekly/{weekStart}")
    public ResponseEntity<?> getWeeklyPlan(@PathVariable String weekStart, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Date weekStartDate = Date.valueOf(weekStart);
            Map<String, Object> result = planService.getWeeklyPlan(userId, weekStartDate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/weekly/save-template")
    public ResponseEntity<?> saveWeeklyTemplate(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String weekStart = (String) request.get("weekStart");
            String templateName = (String) request.get("templateName");
            planService.saveWeeklyTemplate(userId, Date.valueOf(weekStart), templateName);
            return ResponseEntity.ok("模板保存成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/weekly/apply-template")
    public ResponseEntity<?> applyTemplate(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Long templateId = Long.valueOf(request.get("templateId").toString());
            String weekStart = (String) request.get("weekStart");
            Map<String, Object> result = planService.applyTemplate(userId, templateId, Date.valueOf(weekStart));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/weekly/apply")
    public ResponseEntity<?> applyWeeklyPlan(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            String weekStart = (String) request.get("weekStart");
            Map<String, Object> result = planService.applyWeeklyPlan(userId, Date.valueOf(weekStart));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/templates")
    public ResponseEntity<?> getUserTemplates(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            List<WeeklyPlan> templates = planService.getUserTemplates(userId);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/replace-food")
    public ResponseEntity<?> replaceFood(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Long planId = Long.valueOf(request.get("planId").toString());
            Long newFoodId = Long.valueOf(request.get("newFoodId").toString());
            BigDecimal newAmount = new BigDecimal(request.get("newAmount").toString());
            String date = (String) request.get("date"); // 添加日期参数
            Map<String, Object> result = planService.replaceFood(userId, planId, newFoodId, newAmount, date);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/similar-foods/{foodId}")
    public ResponseEntity<?> getSimilarFoods(@PathVariable Long foodId, @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Map<String, Object>> similarFoods = planService.getSimilarFoods(foodId, limit);
            return ResponseEntity.ok(similarFoods);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/regenerate/{date}")
    public ResponseEntity<?> regeneratePlan(@PathVariable String date, @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Date planDate = Date.valueOf(date);
            String requirements = request.get("requirements");
            List<DailyPlan> plans = planService.regeneratePlan(userId, planDate, requirements);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/templates/{templateId}/detail")
    public ResponseEntity<?> getTemplateDetail(@PathVariable Long templateId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            Map<String, Object> result = planService.getTemplateDetail(userId, templateId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/templates/update-food")
    public ResponseEntity<?> updateTemplateFood(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            Long planId = Long.valueOf(request.get("planId").toString());
            Long newFoodId = Long.valueOf(request.get("newFoodId").toString());
            BigDecimal newAmount = new BigDecimal(request.get("newAmount").toString());
            Long templateId = Long.valueOf(request.get("templateId").toString());
            String date = (String) request.get("date");
            Map<String, Object> result = planService.updateTemplateFood(userId, planId, newFoodId, newAmount, templateId, date);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long templateId, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            planService.deleteTemplate(userId, templateId);
            return ResponseEntity.ok("模板删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


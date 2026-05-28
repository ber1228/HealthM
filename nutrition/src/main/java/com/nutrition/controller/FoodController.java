package com.nutrition.controller;

import com.nutrition.entity.Food;
import com.nutrition.mapper.FoodMapper;
import com.nutrition.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/foods")
@CrossOrigin
public class FoodController {
    
    @Autowired
    private FoodMapper foodMapper;
    
    @Autowired
    private AuthUtil authUtil;
    
    @GetMapping
    public ResponseEntity<List<Food>> getAllFoods() {
        List<Food> foods = foodMapper.findAllEnabled();
        return ResponseEntity.ok(foods);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Food>> getFoodsByCategory(@PathVariable String category) {
        List<Food> foods = foodMapper.findByCategoryEnabled(category);
        return ResponseEntity.ok(foods);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Food> getFoodById(@PathVariable Long id) {
        Food food = foodMapper.findById(id);
        if (food == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(food);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Food>> searchFoods(@RequestParam("keyword") String keyword) {
        List<Food> foods = foodMapper.searchByNameEnabled(keyword);
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/page")
    public ResponseEntity<?> pageFoods(@RequestParam(value = "keyword", required = false) String keyword,
                                       @RequestParam(value = "category", required = false) String category,
                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        int limit = Math.max(size, 1);
        List<Food> items = foodMapper.queryFoodsEnabled(keyword, category, offset, limit);
        int total = foodMapper.countFoodsEnabled(keyword, category);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("total", total);
        resp.put("page", page);
        resp.put("size", size);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<Food>> recommendations(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        List<Food> foods = foodMapper.topByProteinEnabled(Math.min(Math.max(limit, 1), 20));
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/admin/list")
    public ResponseEntity<?> adminList(HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        List<Food> foods = foodMapper.findAll();
        return ResponseEntity.ok(foods);
    }

    @PostMapping
    public ResponseEntity<?> createFood(@RequestBody Food food, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            foodMapper.insert(food);
            return ResponseEntity.ok(food);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFood(@PathVariable Long id, @RequestBody Food food, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            food.setId(id);
            int rows = foodMapper.update(food);
            if (rows > 0) {
                return ResponseEntity.ok(foodMapper.findById(id));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFood(@PathVariable Long id, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            int rows = foodMapper.deleteById(id);
            if (rows > 0) {
                return ResponseEntity.ok("删除成功");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }
}


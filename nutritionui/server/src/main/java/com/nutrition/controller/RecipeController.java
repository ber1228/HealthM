package com.nutrition.controller;

import com.nutrition.entity.Recipe;
import com.nutrition.entity.RecipeIngredient;
import com.nutrition.entity.RecipeStep;
import com.nutrition.entity.RecipeComment;
import com.nutrition.mapper.RecipeMapper;
import com.nutrition.mapper.RecipeIngredientMapper;
import com.nutrition.mapper.RecipeStepMapper;
import com.nutrition.mapper.RecipeCommentMapper;
import com.nutrition.mapper.UserMapper;
import com.nutrition.entity.User;
import com.nutrition.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/recipes")
@CrossOrigin
public class RecipeController {

    @Autowired
    private RecipeMapper recipeMapper;
    @Autowired
    private RecipeIngredientMapper ingredientMapper;
    @Autowired
    private RecipeStepMapper stepMapper;
    @Autowired
    private RecipeCommentMapper commentMapper;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private UserMapper userMapper;

    // 用户端：分页查询
    @GetMapping("/page")
    public ResponseEntity<?> page(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "difficulty", required = false) String difficulty,
            @RequestParam(value = "taste", required = false) String taste,
            @RequestParam(value = "minDuration", required = false) Integer minDuration,
            @RequestParam(value = "maxDuration", required = false) Integer maxDuration,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "12") int size
    ) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<Recipe> items = recipeMapper.page(keyword, category, difficulty, taste, minDuration, maxDuration, true, offset, size);
        int total = recipeMapper.count(keyword, category, difficulty, taste, minDuration, maxDuration, true);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("total", total);
        resp.put("page", page);
        resp.put("size", size);
        return ResponseEntity.ok(resp);
    }

    // 用户端：详情
    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        Recipe recipe = recipeMapper.findById(id);
        if (recipe == null || (recipe.getPublished() != null && !recipe.getPublished())) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> resp = buildRecipeDetail(id, recipe);
        return ResponseEntity.ok(resp);
    }

    private Map<String, Object> buildRecipeDetail(Long id, Recipe recipe) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("recipe", recipe);
        resp.put("ingredients", ingredientMapper.findByRecipeId(id));
        resp.put("steps", stepMapper.findByRecipeId(id));
        resp.put("comments", commentMapper.findByRecipeId(id, 100));
        return resp;
    }

    // 管理端：分页（含未发布）
    @GetMapping("/admin/page")
    public ResponseEntity<?> adminPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "difficulty", required = false) String difficulty,
            @RequestParam(value = "taste", required = false) String taste,
            @RequestParam(value = "minDuration", required = false) Integer minDuration,
            @RequestParam(value = "maxDuration", required = false) Integer maxDuration,
            @RequestParam(value = "published", required = false) Boolean published,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            HttpServletRequest request
    ) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<Recipe> items = recipeMapper.page(keyword, category, difficulty, taste, minDuration, maxDuration, published, offset, size);
        int total = recipeMapper.count(keyword, category, difficulty, taste, minDuration, maxDuration, published);
        Map<String, Object> resp = new HashMap<>();
        resp.put("items", items);
        resp.put("total", total);
        resp.put("page", page);
        resp.put("size", size);
        return ResponseEntity.ok(resp);
    }

    // 管理端：创建/更新（含主辅料和步骤）
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            Recipe recipe = mapToRecipe(body);
            if (recipe.getPublished() == null) recipe.setPublished(false);
            recipeMapper.insert(recipe);
            Long id = recipe.getId();
            saveIngredientsAndSteps(id, body);
            return ResponseEntity.ok(buildRecipeDetail(id, recipeMapper.findById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            Recipe recipe = mapToRecipe(body);
            recipe.setId(id);
            recipeMapper.update(recipe);
            ingredientMapper.deleteByRecipeId(id);
            stepMapper.deleteByRecipeId(id);
            saveIngredientsAndSteps(id, body);
            return ResponseEntity.ok(buildRecipeDetail(id, recipeMapper.findById(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            recipeMapper.deleteById(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable Long id, @RequestParam("published") boolean published, HttpServletRequest request) {
        if (!authUtil.isAdmin(request)) {
            return ResponseEntity.status(403).body("权限不足，需要管理员权限");
        }
        try {
            Recipe recipe = recipeMapper.findById(id);
            if (recipe == null) return ResponseEntity.notFound().build();
            recipe.setPublished(published);
            recipeMapper.update(recipe);
            return ResponseEntity.ok("状态已更新");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body("未登录，无法发表评论");
            }
            User user = userMapper.findById(userId);
            String username = user != null ? user.getUsername() : "用户" + userId;

            RecipeComment comment = new RecipeComment();
            comment.setRecipeId(id);
            comment.setUserId(userId);
            comment.setUsername(username);
            comment.setContent(body.get("content"));
            commentMapper.insert(comment);
            return ResponseEntity.ok("评论成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("评论失败: " + e.getMessage());
        }
    }

    private void saveIngredientsAndSteps(Long id, Map<String, Object> body) {
        List<Map<String, Object>> mains = (List<Map<String, Object>>) body.getOrDefault("mains", Collections.emptyList());
        List<Map<String, Object>> auxes = (List<Map<String, Object>>) body.getOrDefault("auxes", Collections.emptyList());
        List<Map<String, Object>> steps = (List<Map<String, Object>>) body.getOrDefault("steps", Collections.emptyList());
        for (Map<String, Object> m : mains) {
            RecipeIngredient ing = new RecipeIngredient();
            ing.setRecipeId(id);
            ing.setType("main");
            ing.setName((String) m.get("name"));
            ing.setAmount((String) m.get("amount"));
            ingredientMapper.insert(ing);
        }
        for (Map<String, Object> a : auxes) {
            RecipeIngredient ing = new RecipeIngredient();
            ing.setRecipeId(id);
            ing.setType("aux");
            ing.setName((String) a.get("name"));
            ing.setAmount((String) a.get("amount"));
            ingredientMapper.insert(ing);
        }
        int idx = 1;
        for (Map<String, Object> s : steps) {
            RecipeStep step = new RecipeStep();
            step.setRecipeId(id);
            Object orderObj = s.get("stepOrder");
            step.setStepOrder(orderObj == null ? idx : Integer.parseInt(orderObj.toString()));
            step.setDescription((String) s.get("description"));
            step.setImageUrl((String) s.get("imageUrl"));
            Object isKey = s.get("isKey");
            step.setIsKey(isKey != null && ("1".equals(isKey.toString()) || Boolean.parseBoolean(isKey.toString())));
            stepMapper.insert(step);
            idx++;
        }
    }

    private Recipe mapToRecipe(Map<String, Object> body) {
        Recipe r = new Recipe();
        r.setName((String) body.get("name"));
        r.setAuthor((String) body.get("author"));
        r.setCategory((String) body.get("category"));
        Object dm = body.get("durationMinutes");
        r.setDurationMinutes(dm == null ? null : Integer.parseInt(dm.toString()));
        r.setDifficulty((String) body.get("difficulty"));
        r.setTaste((String) body.get("taste"));
        r.setCoverImageUrl((String) body.get("coverImageUrl"));
        if (body.get("calories") != null) {
            try { r.setCalories(new java.math.BigDecimal(body.get("calories").toString())); } catch (Exception ignored) {}
        }
        Object pub = body.get("published");
        r.setPublished(pub != null && ("1".equals(pub.toString()) || Boolean.parseBoolean(pub.toString())));
        return r;
    }
}

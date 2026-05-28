package com.nutrition.controller;

import com.nutrition.entity.Food;
import com.nutrition.entity.MealRecord;
import com.nutrition.mapper.FoodMapper;
import com.nutrition.mapper.MealRecordMapper;
import com.nutrition.service.MultimodalAIService;
import com.nutrition.util.FoodMatcher;
import com.nutrition.util.NutritionCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.*;

@RestController
@RequestMapping("/food-recognition")
@CrossOrigin
public class FoodRecognitionController {

    @Autowired
    private MultimodalAIService multimodalAIService;

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private MealRecordMapper mealRecordMapper;

    /**
     * POST /food-recognition/photo
     * Analyze a food photo and return recognized food items with nutrition data.
     */
    @PostMapping("/photo")
    public ResponseEntity<?> analyzePhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mealType", defaultValue = "lunch") String mealType,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "用户未登录"));
            }

            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "请上传图片"));
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "仅支持图片格式"));
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "图片大小不能超过10MB"));
            }

            // Convert to base64
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            // Call AI for photo analysis
            List<Map<String, Object>> aiResults = multimodalAIService.analyzeFoodPhoto(base64);

            // Match against foods table and build response
            List<Food> allFoods = foodMapper.findAllEnabled();
            List<Map<String, Object>> foods = buildFoodResults(aiResults, allFoods);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("foods", foods);
            response.put("mealType", mealType);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("图片识别异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "图片识别失败: " + e.getMessage()));
        }
    }

    /**
     * POST /food-recognition/voice
     * Parse voice-transcribed text and return recognized food items with nutrition data.
     */
    @PostMapping("/voice")
    public ResponseEntity<?> analyzeVoiceText(@RequestBody Map<String, String> request,
                                               HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "用户未登录"));
            }

            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "语音内容不能为空"));
            }

            // Call AI for text parsing
            List<Map<String, Object>> aiResults = multimodalAIService.parseFoodFromText(text);

            // Match against foods table
            List<Food> allFoods = foodMapper.findAllEnabled();
            List<Map<String, Object>> foods = buildFoodResults(aiResults, allFoods);

            // Infer mealType from AI results
            String mealType = "lunch";
            if (!aiResults.isEmpty() && aiResults.get(0).containsKey("mealType")) {
                String aiMealType = aiResults.get(0).get("mealType").toString();
                if (isValidMealType(aiMealType)) {
                    mealType = aiMealType;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("foods", foods);
            response.put("mealType", mealType);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("语音解析异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "语音解析失败: " + e.getMessage()));
        }
    }

    /**
     * POST /food-recognition/confirm
     * Save confirmed food records to the meal_records table.
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmAndSave(@RequestBody Map<String, Object> request,
                                             HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "用户未登录"));
            }

            String mealType = (String) request.get("mealType");
            if (mealType == null || mealType.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "请选择餐次"));
            }

            // Normalize snack types
            if ("snack".equals(mealType)) {
                mealType = inferSnackType();
            }

            String dateStr = (String) request.get("date");
            Date date;
            if (dateStr != null && !dateStr.isEmpty()) {
                date = Date.valueOf(dateStr);
            } else {
                date = new Date(System.currentTimeMillis());
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> foods = (List<Map<String, Object>>) request.get("foods");
            if (foods == null || foods.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "食物列表不能为空"));
            }

            List<MealRecord> records = new ArrayList<>();

            for (Map<String, Object> foodItem : foods) {
                String foodName = (String) foodItem.get("name");
                Object weightObj = foodItem.get("weight");
                BigDecimal weight = weightObj instanceof Number
                        ? BigDecimal.valueOf(((Number) weightObj).doubleValue())
                        : new BigDecimal(weightObj.toString());

                if (foodName == null || foodName.isEmpty() || weight.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                Object foodIdObj = foodItem.get("foodId");
                if (foodIdObj != null) {
                    // Matched food - use database nutrition
                    Long foodId = Long.valueOf(foodIdObj.toString());
                    Food food = foodMapper.findById(foodId);
                    if (food != null) {
                        MealRecord record = NutritionCalculator.calculate(food, weight, userId, date, mealType);
                        records.add(record);
                        continue;
                    }
                }

                // Unmatched food - use AI-estimated nutrition from frontend
                MealRecord record = new MealRecord();
                record.setUserId(userId);
                record.setDate(date);
                record.setMealType(mealType);
                record.setFoodName(foodName);
                record.setAmount(weight);

                // Extract nutrition values from the request
                @SuppressWarnings("unchecked")
                Map<String, Object> nutrition = (Map<String, Object>) foodItem.get("nutrition");
                if (nutrition != null) {
                    record.setCalories(getBigDecimal(nutrition, "calories"));
                    record.setProtein(getBigDecimal(nutrition, "protein"));
                    record.setCarbs(getBigDecimal(nutrition, "carbs"));
                    record.setFat(getBigDecimal(nutrition, "fat"));
                    record.setFiber(getBigDecimal(nutrition, "fiber"));
                    record.setCalcium(getBigDecimal(nutrition, "calcium"));
                    record.setIron(getBigDecimal(nutrition, "iron"));
                    record.setVitc(getBigDecimal(nutrition, "vitc"));
                    record.setPotassium(getBigDecimal(nutrition, "potassium"));
                    record.setSodium(getBigDecimal(nutrition, "sodium"));
                }

                // Try to insert as a new food for future matching
                try {
                    Food newFood = createFoodFromEstimate(foodName, weight, nutrition);
                    foodMapper.insert(newFood);
                    record.setFoodId(newFood.getId());
                } catch (Exception e) {
                    System.err.println("创建新食材失败: " + e.getMessage());
                }

                records.add(record);
            }

            if (records.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "没有有效的食物记录"));
            }

            // Batch insert
            mealRecordMapper.batchInsert(records);

            return ResponseEntity.ok(Map.of("success", true, "message", "保存成功", "count", records.size()));
        } catch (Exception e) {
            System.err.println("保存记录异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "保存失败: " + e.getMessage()));
        }
    }

    /**
     * Build food results by matching AI results against the foods table.
     */
    private List<Map<String, Object>> buildFoodResults(List<Map<String, Object>> aiResults, List<Food> allFoods) {
        List<Map<String, Object>> foods = new ArrayList<>();

        for (Map<String, Object> aiItem : aiResults) {
            String name = aiItem.get("name") != null ? aiItem.get("name").toString() : "";
            Object weightObj = aiItem.get("weight");
            double weight = weightObj instanceof Number
                    ? ((Number) weightObj).doubleValue()
                    : 100;

            Map<String, Object> foodResult = new HashMap<>();
            foodResult.put("name", name);
            foodResult.put("weight", weight);

            // Try to match against foods table
            Food matched = FoodMatcher.findBestMatch(name, allFoods);
            if (matched != null) {
                foodResult.put("matched", true);
                foodResult.put("foodId", matched.getId());
                foodResult.put("matchedName", matched.getName());

                // Calculate nutrition for this weight
                BigDecimal amount = BigDecimal.valueOf(weight);
                BigDecimal divisor = BigDecimal.valueOf(100);
                Map<String, Object> nutrition = new HashMap<>();
                nutrition.put("calories", safeCalc(matched.getCalories(), amount, divisor));
                nutrition.put("protein", safeCalc(matched.getProtein(), amount, divisor));
                nutrition.put("carbs", safeCalc(matched.getCarbs(), amount, divisor));
                nutrition.put("fat", safeCalc(matched.getFat(), amount, divisor));
                nutrition.put("fiber", safeCalc(matched.getFiber(), amount, divisor));
                nutrition.put("calcium", safeCalc(matched.getCalcium(), amount, divisor));
                nutrition.put("iron", safeCalc(matched.getIron(), amount, divisor));
                nutrition.put("vitc", safeCalc(matched.getVitc(), amount, divisor));
                nutrition.put("potassium", safeCalc(matched.getPotassium(), amount, divisor));
                nutrition.put("sodium", safeCalc(matched.getSodium(), amount, divisor));
                foodResult.put("nutrition", nutrition);
            } else {
                foodResult.put("matched", false);
                // Use AI-estimated nutrition if available in the response
                if (aiItem.containsKey("calories")) {
                    Map<String, Object> nutrition = new HashMap<>();
                    nutrition.put("calories", getNumberValue(aiItem, "calories"));
                    nutrition.put("protein", getNumberValue(aiItem, "protein"));
                    nutrition.put("carbs", getNumberValue(aiItem, "carbs"));
                    nutrition.put("fat", getNumberValue(aiItem, "fat"));
                    nutrition.put("fiber", getNumberValue(aiItem, "fiber"));
                    nutrition.put("calcium", getNumberValue(aiItem, "calcium"));
                    nutrition.put("iron", getNumberValue(aiItem, "iron"));
                    nutrition.put("vitc", getNumberValue(aiItem, "vitc"));
                    nutrition.put("potassium", getNumberValue(aiItem, "potassium"));
                    nutrition.put("sodium", getNumberValue(aiItem, "sodium"));
                    foodResult.put("nutrition", nutrition);
                }
            }

            foods.add(foodResult);
        }

        return foods;
    }

    private double safeCalc(BigDecimal value, BigDecimal amount, BigDecimal divisor) {
        if (value == null) return 0;
        return value.multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP).doubleValue();
    }

    private double getNumberValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return 0;
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private Food createFoodFromEstimate(String name, BigDecimal weight, Map<String, Object> nutrition) {
        Food food = new Food();
        food.setName(name);
        food.setCategory("其他");
        food.setAvailability("enabled");

        if (nutrition != null) {
            // Convert nutrition values to per-100g
            BigDecimal factor = BigDecimal.valueOf(100).divide(weight, 4, RoundingMode.HALF_UP);
            food.setCalories(scaledValue(nutrition.get("calories"), factor));
            food.setProtein(scaledValue(nutrition.get("protein"), factor));
            food.setCarbs(scaledValue(nutrition.get("carbs"), factor));
            food.setFat(scaledValue(nutrition.get("fat"), factor));
            food.setFiber(scaledValue(nutrition.get("fiber"), factor));
            food.setCalcium(scaledValue(nutrition.get("calcium"), factor));
            food.setIron(scaledValue(nutrition.get("iron"), factor));
            food.setVitc(scaledValue(nutrition.get("vitc"), factor));
            food.setPotassium(scaledValue(nutrition.get("potassium"), factor));
            food.setSodium(scaledValue(nutrition.get("sodium"), factor));
        }

        return food;
    }

    private BigDecimal scaledValue(Object val, BigDecimal factor) {
        if (val == null) return BigDecimal.ZERO;
        BigDecimal bd;
        if (val instanceof BigDecimal) bd = (BigDecimal) val;
        else if (val instanceof Number) bd = BigDecimal.valueOf(((Number) val).doubleValue());
        else return BigDecimal.ZERO;
        return bd.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isValidMealType(String type) {
        return "breakfast".equals(type) || "lunch".equals(type) || "dinner".equals(type)
                || "morning_snack".equals(type) || "afternoon_snack".equals(type) || "evening_snack".equals(type);
    }

    private String inferSnackType() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "morning_snack";
        if (hour < 17) return "afternoon_snack";
        return "evening_snack";
    }
}

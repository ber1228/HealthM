package com.nutrition.controller;

import com.nutrition.entity.Food;
import com.nutrition.entity.MealRecord;
import com.nutrition.entity.NutritionAnalysis;
import com.nutrition.mapper.FoodMapper;
import com.nutrition.mapper.MealRecordMapper;
import com.nutrition.service.MealRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.*;

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
                record.setAmount(new BigDecimal(requestData.get("amount").toString()));
            } else {
                return ResponseEntity.badRequest().body("数量不能为空");
            }

            if (record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body("数量必须大于0");
            }

            Food food = foodMapper.findById(record.getFoodId());
            if (food != null) {
                BigDecimal amount = record.getAmount();
                BigDecimal divisor = BigDecimal.valueOf(100);

                if (food.getCalories() != null) {
                    record.setCalories(food.getCalories().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getProtein() != null) {
                    record.setProtein(food.getProtein().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getCarbs() != null) {
                    record.setCarbs(food.getCarbs().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getFat() != null) {
                    record.setFat(food.getFat().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getFiber() != null) {
                    record.setFiber(food.getFiber().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getCalcium() != null) {
                    record.setCalcium(food.getCalcium().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getIron() != null) {
                    record.setIron(food.getIron().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getVitc() != null) {
                    record.setVitc(food.getVitc().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getPotassium() != null) {
                    record.setPotassium(food.getPotassium().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
                }
                if (food.getSodium() != null) {
                    record.setSodium(food.getSodium().multiply(amount).divide(divisor, 2, RoundingMode.HALF_UP));
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


    // 统一营养分析接口 - 只支持日维度
    @GetMapping("/nutrition/analysis")
    public ResponseEntity<?> getNutritionAnalysis(@RequestParam("date") String dateStr, HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            System.out.println("RecordController - getNutritionAnalysis - userId: " + userId + ", dateStr: " + dateStr);

            Map<String, Object> result = new HashMap<>();

            // 解析日期字符串，支持多种格式
            Date dateObj = parseDate(dateStr);

            if (dateObj == null) {
                throw new RuntimeException("日期格式无效，请输入正确的日期");
            }

            try {
                Map<String, Object> dailyReport = mealRecordService.analyzeDailyRecords(userId, dateObj);
                result.put("indicators", formatDailyIndicators(dailyReport));
                result.put("chartData", generateDailyChartData(dailyReport));
                result.put("report", generateDailyReport(dailyReport));
            } catch (RuntimeException e) {
                // 如果当天没有记录，返回空数据而不是错误
                System.out.println("RecordController - 当天没有饮食记录，返回空数据: " + e.getMessage());

                // 返回空数据
                result.put("indicators", new ArrayList<>());
                result.put("chartInstanceData", new HashMap<>());
                result.put("report", new HashMap<>());
            }

            // 包装响应格式，符合前端期望
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", result);

            System.out.println("RecordController - 返回响应数据: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("RecordController - 获取营养分析数据异常: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 400);
            errorResponse.put("msg", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // 添加日期解析方法
    private Date parseDate(String dateStr) {
        try {
            System.out.println("RecordController - parseDate - 原始输入: " + dateStr);

            // 如果是ISO格式（带T和Z），提取日期部分
            if (dateStr.contains("T")) {
                // 提取YYYY-MM-DD部分
                String datePart = dateStr.split("T")[0];
                System.out.println("RecordController - 提取日期部分: " + datePart);

                // 验证日期格式
                if (datePart.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    Date result = Date.valueOf(datePart);
                    System.out.println("RecordController - 解析结果: " + result);
                    return result;
                }
            }

            // 直接尝试转换
            Date result = Date.valueOf(dateStr);
            System.out.println("RecordController - 直接解析结果: " + result);
            return result;

        } catch (Exception e) {
            System.out.println("RecordController - 日期解析失败: " + dateStr + ", 错误: " + e.getMessage());
            return null;
        }
    }
    // 格式化日分析指标
    private List<Map<String, Object>> formatDailyIndicators(Map<String, Object> report) {
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> total = (Map<String, BigDecimal>) report.get("totalNutrition");
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> target = (Map<String, BigDecimal>) report.get("targetNutrition");

        List<Map<String, Object>> indicators = new ArrayList<>();

        // 定义需要显示的核心营养素
        List<String> nutrients = Arrays.asList("calories", "protein", "carbs", "fat", "fiber", "calcium", "iron", "vitc", "potassium", "sodium");
        List<String> nutrientNames = Arrays.asList("热量", "蛋白质", "碳水化合物", "脂肪", "膳食纤维", "钙", "铁", "维生素C", "钾", "钠");
        List<String> units = Arrays.asList("kcal", "g", "g", "g", "g", "mg", "mg", "mg", "mg", "mg");

        for (int i = 0; i < nutrients.size(); i++) {
            String key = nutrients.get(i);
            BigDecimal actual = total.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal targetVal = target.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal gap = actual.subtract(targetVal);
            int rate = targetVal.compareTo(BigDecimal.ZERO) > 0 ? actual.multiply(new BigDecimal(100)).divide(targetVal, 0, RoundingMode.HALF_UP).intValue() : 0;

            Map<String, Object> indicator = new HashMap<>();
            indicator.put("name", nutrientNames.get(i));
            indicator.put("unit", units.get(i));
            indicator.put("actual", actual.setScale(1, RoundingMode.HALF_UP).toString());
            indicator.put("target", targetVal.setScale(1, RoundingMode.HALF_UP).toString());
            indicator.put("gap", gap.setScale(1, RoundingMode.HALF_UP).toString());
            indicator.put("rate", rate);

            indicators.add(indicator);
        }

        return indicators;
    }

    // 生成日分析图表数据
    private Map<String, Object> generateDailyChartData(Map<String, Object> report) {
        @SuppressWarnings("unchecked")
        List<MealRecord> records = (List<MealRecord>) report.get("records");

        // 定义餐次类型中英文映射
        Map<String, String> mealTypeNameMap = new HashMap<>();
        mealTypeNameMap.put("breakfast", "早餐");
        mealTypeNameMap.put("lunch", "午餐");
        mealTypeNameMap.put("dinner", "晚餐");
        mealTypeNameMap.put("morning_snack", "加餐");
        mealTypeNameMap.put("afternoon_snack", "加餐");
        mealTypeNameMap.put("evening_snack", "加餐");

        // 按餐次分组统计 - 用于饼图
        Map<String, Map<String, BigDecimal>> mealMap = new HashMap<>();
        mealMap.put("早餐", new HashMap<>());
        mealMap.put("午餐", new HashMap<>());
        mealMap.put("晚餐", new HashMap<>());
        mealMap.put("加餐", new HashMap<>());

        // 按营养素分类统计 - 用于第二个饼图
        Map<String, BigDecimal> nutrientMap = new HashMap<>();
        nutrientMap.put("蛋白质", BigDecimal.ZERO);
        nutrientMap.put("碳水化合物", BigDecimal.ZERO);
        nutrientMap.put("脂肪", BigDecimal.ZERO);
        nutrientMap.put("其他营养素", BigDecimal.ZERO);

        // 初始化各餐次营养素为0
        for (Map<String, BigDecimal> mealNutrition : mealMap.values()) {
            mealNutrition.put("calories", BigDecimal.ZERO);
            mealNutrition.put("protein", BigDecimal.ZERO);
            mealNutrition.put("carbs", BigDecimal.ZERO);
            mealNutrition.put("fat", BigDecimal.ZERO);
            mealNutrition.put("other", BigDecimal.ZERO); // 其他营养素
        }

        // 统计各餐次营养
        for (MealRecord record : records) {
            String mealType = record.getMealType();
            // 将英文餐次类型转换为中文
            String chineseMealType = mealTypeNameMap.getOrDefault(mealType, "加餐");
            Map<String, BigDecimal> mealNutrition = mealMap.get(chineseMealType);

            // 更新餐次总热量
            BigDecimal mealCalories = mealNutrition.getOrDefault("calories", BigDecimal.ZERO)
                    .add(record.getCalories());
            mealNutrition.put("calories", mealCalories);

            // 更新营养素分类统计
            BigDecimal protein = record.getProtein() != null ? record.getProtein() : BigDecimal.ZERO;
            BigDecimal carbs = record.getCarbs() != null ? record.getCarbs() : BigDecimal.ZERO;
            BigDecimal fat = record.getFat() != null ? record.getFat() : BigDecimal.ZERO;

            // 计算其他营养素（总热量减去碳蛋脂的热量）
            BigDecimal proteinCalories = protein.multiply(new BigDecimal("4")); // 1g蛋白质=4kcal
            BigDecimal carbsCalories = carbs.multiply(new BigDecimal("4")); // 1g碳水=4kcal
            BigDecimal fatCalories = fat.multiply(new BigDecimal("9")); // 1g脂肪=9kcal

            BigDecimal otherCalories = record.getCalories()
                    .subtract(proteinCalories)
                    .subtract(carbsCalories)
                    .subtract(fatCalories);
            otherCalories = otherCalories.compareTo(BigDecimal.ZERO) < 0 ?
                    BigDecimal.ZERO : otherCalories;

            // 更新营养素分类
            nutrientMap.put("蛋白质", nutrientMap.get("蛋白质").add(protein));
            nutrientMap.put("碳水化合物", nutrientMap.get("碳水化合物").add(carbs));
            nutrientMap.put("脂肪", nutrientMap.get("脂肪").add(fat));
            nutrientMap.put("其他营养素", nutrientMap.get("其他营养素").add(otherCalories.divide(new BigDecimal("9"), 2, RoundingMode.HALF_UP)));
        }

        // 第一个饼图：各餐次热量占比
        List<Map<String, Object>> mealPieData = new ArrayList<>();
        BigDecimal totalCalories = BigDecimal.ZERO;

        // 计算总热量
        for (Map<String, BigDecimal> nutrition : mealMap.values()) {
            totalCalories = totalCalories.add(nutrition.getOrDefault("calories", BigDecimal.ZERO));
        }

        // 构造餐次饼图数据
        for (Map.Entry<String, Map<String, BigDecimal>> entry : mealMap.entrySet()) {
            String mealType = entry.getKey();
            Map<String, BigDecimal> nutrition = entry.getValue();
            BigDecimal calories = nutrition.getOrDefault("calories", BigDecimal.ZERO);

            if (calories.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> dataItem = new HashMap<>();
                dataItem.put("name", mealType);
                dataItem.put("value", calories);
                mealPieData.add(dataItem);
            }
        }

        // 第二个饼图：碳蛋脂+其他营养素占比（按重量g计算，需要转换为热量占比）
        List<Map<String, Object>> nutrientPieData = new ArrayList<>();

        // 计算营养素总重量
        BigDecimal totalProteinGrams = nutrientMap.get("蛋白质");
        BigDecimal totalCarbsGrams = nutrientMap.get("碳水化合物");
        BigDecimal totalFatGrams = nutrientMap.get("脂肪");
        BigDecimal totalOtherGrams = nutrientMap.get("其他营养素");

        // 转换为热量
        BigDecimal proteinCalories = totalProteinGrams.multiply(new BigDecimal("4"));
        BigDecimal carbsCalories = totalCarbsGrams.multiply(new BigDecimal("4"));
        BigDecimal fatCalories = totalFatGrams.multiply(new BigDecimal("9"));
        BigDecimal otherCalories = totalOtherGrams.multiply(new BigDecimal("9"));

        BigDecimal totalNutrientCalories = proteinCalories
                .add(carbsCalories)
                .add(fatCalories)
                .add(otherCalories);

        // 构造营养素饼图数据
        if (proteinCalories.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "蛋白质");
            map.put("value", proteinCalories);
            nutrientPieData.add(map);
        }
        if (carbsCalories.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "碳水化合物");
            map.put("value", carbsCalories);
            nutrientPieData.add(map);
        }
        if (fatCalories.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "脂肪");
            map.put("value", fatCalories);
            nutrientPieData.add(map);
        }
        if (otherCalories.compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "其他营养素");
            map.put("value", otherCalories);
            nutrientPieData.add(map);
        }

        // 返回两个饼图的数据结构
        Map<String, Object> chartData = new HashMap<>();

        // 餐次饼图数据
        Map<String, Object> mealChart = new HashMap<>();
        mealChart.put("title", "当日各餐次热量占比");
        mealChart.put("seriesData", mealPieData);
        mealChart.put("chartType", "meal");

        // 营养素饼图数据
        Map<String, Object> nutrientChart = new HashMap<>();
        nutrientChart.put("title", "营养素热量构成");
        nutrientChart.put("seriesData", nutrientPieData);
        nutrientChart.put("chartType", "nutrient");

        chartData.put("mealChart", mealChart);
        chartData.put("nutrientChart", nutrientChart);

        return chartData;
    }

    // 生成日分析报告
    private Map<String, Object> generateDailyReport(Map<String, Object> report) {
        String analysisResult = (String) report.get("analysisResult");

        @SuppressWarnings("unchecked")
        Map<String, Object> gapReport = (Map<String, Object>) report.get("gapReport");

        // 生成详细分析报告
        List<Map<String, Object>> details = new ArrayList<>();

        // 定义营养素名称映射
        Map<String, String> nutrientNames = new HashMap<>();
        nutrientNames.put("calories", "热量");
        nutrientNames.put("protein", "蛋白质");
        nutrientNames.put("carbs", "碳水化合物");
        nutrientNames.put("fat", "脂肪");
        nutrientNames.put("fiber", "膳食纤维");
        nutrientNames.put("calcium", "钙");
        nutrientNames.put("iron", "铁");
        nutrientNames.put("vitc", "维生素C");
        nutrientNames.put("potassium", "钾");
        nutrientNames.put("sodium", "钠");

        for (String nutrient : gapReport.keySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) gapReport.get(nutrient);

            Map<String, Object> detail = new HashMap<>();
            detail.put("item", nutrientNames.getOrDefault(nutrient, nutrient));

            String status = (String) info.get("status");
            String statusText = "正常";
            String statusType = "success";

            if ("deficient".equals(status)) {
                statusText = "不足";
                statusType = "warning";
            } else if ("excess".equals(status)) {
                statusText = "超标";
                statusType = "danger";
            }

            detail.put("status", statusText);
            detail.put("statusType", statusType);
            detail.put("suggestion", info.get("suggestion"));

            details.add(detail);
        }

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("summary", analysisResult);
        reportData.put("details", details);

        return reportData;
    }

}
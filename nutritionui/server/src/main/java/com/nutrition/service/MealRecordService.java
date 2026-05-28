package com.nutrition.service;

import com.nutrition.entity.MealRecord;
import com.nutrition.entity.NutritionAnalysis;
import com.nutrition.entity.User;
import com.nutrition.entity.DailyPlan;
import com.nutrition.mapper.MealRecordMapper;
import com.nutrition.mapper.NutritionAnalysisMapper;
import com.nutrition.mapper.UserMapper;
import com.nutrition.mapper.DailyPlanMapper;
import com.nutrition.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MealRecordService {
    
    @Autowired
    private MealRecordMapper mealRecordMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NutritionAnalysisMapper nutritionAnalysisMapper;
    
    @Autowired
    private DailyPlanMapper dailyPlanMapper;
    
    @Autowired
    private MessageService messageService;
    
    public void addRecord(MealRecord record) {
        mealRecordMapper.insert(record);
    }
    
    public List<MealRecord> getRecordsByDate(Long userId, Date date) {
        return mealRecordMapper.findByUserAndDate(userId, date);
    }
    
    public void deleteRecord(Long id) {
        mealRecordMapper.delete(id);
    }
    
    // 分析每日饮食记录
    public Map<String, Object> analyzeDailyRecords(Long userId, Date date) {
        System.out.println("MealRecordService - analyzeDailyRecords - userId: " + userId + ", date: " + date);
        List<MealRecord> records = getRecordsByDate(userId, date);
        System.out.println("MealRecordService - 查询到的记录数: " + records.size());
        
        if (records == null || records.isEmpty()) {
            System.out.println("MealRecordService - 没有找到记录");
            throw new RuntimeException("该日期没有饮食记录");
        }
        
        User user = userMapper.findById(userId);
        if (user == null) {
            System.out.println("MealRecordService - 用户不存在");
            throw new RuntimeException("用户不存在");
        }
        System.out.println("MealRecordService - 用户信息: " + user.getUsername());
        
        // 计算总营养摄入
        Map<String, BigDecimal> totalNutrition = calculateTotalNutrition(records);
        
        // 获取目标营养摄入
        Map<String, BigDecimal> targetNutrition = calculateTargetNutrition(user);
        
        // 生成营养缺口报告
        Map<String, Object> gapReport = generateGapReport(totalNutrition, targetNutrition);
        
        // 生成分析结果
        String analysisResult = generateAnalysisResult(totalNutrition, targetNutrition, user.getHealthGoal());
        
        // 保存分析结果
        NutritionAnalysis analysis = new NutritionAnalysis();
        analysis.setUserId(userId);
        analysis.setDate(date);
        analysis.setAnalysisType("report");
        analysis.setTotalCalories(totalNutrition.get("calories"));
        analysis.setTotalProtein(totalNutrition.get("protein"));
        analysis.setTotalCarbs(totalNutrition.get("carbs"));
        analysis.setTotalFat(totalNutrition.get("fat"));
        analysis.setTotalFiber(totalNutrition.get("fiber"));
        analysis.setTotalCalcium(totalNutrition.get("calcium"));
        analysis.setTotalIron(totalNutrition.get("iron"));
        analysis.setTotalVitc(totalNutrition.get("vitc"));
        analysis.setTotalPotassium(totalNutrition.get("potassium"));
        analysis.setTotalSodium(totalNutrition.get("sodium"));
        analysis.setTargetCalories(targetNutrition.get("calories"));
        analysis.setTargetProtein(targetNutrition.get("protein"));
        analysis.setTargetCarbs(targetNutrition.get("carbs"));
        analysis.setTargetFat(targetNutrition.get("fat"));
        analysis.setAnalysisResult(analysisResult);
        analysis.setGapReport(gapReport.toString());
        
        nutritionAnalysisMapper.insert(analysis);
        
        // 检查营养不足或超标，自动发送提醒
        checkAndSendNutritionReminders(userId, gapReport);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalNutrition", totalNutrition);
        result.put("targetNutrition", targetNutrition);
        result.put("gapReport", gapReport);
        result.put("analysisResult", analysisResult);
        result.put("records", records);
        
        return result;
    }
    
    // 分析饮食方案
    public Map<String, Object> analyzeDailyPlan(Long userId, Date date) {
        User user = userMapper.findById(userId);
        
        // 从DailyPlan获取实际数据
        List<DailyPlan> plans = dailyPlanMapper.findByUserAndDate(userId, date);
        
        if (plans.isEmpty()) {
            throw new RuntimeException("该日期没有饮食方案");
        }
        
        // 计算方案总营养
        Map<String, BigDecimal> planNutrition = calculatePlanNutrition(plans);
        
        Map<String, BigDecimal> targetNutrition = calculateTargetNutrition(user);
        Map<String, Object> gapReport = generateGapReport(planNutrition, targetNutrition);
        String analysisResult = generateAnalysisResult(planNutrition, targetNutrition, user.getHealthGoal());
        
        Map<String, Object> result = new HashMap<>();
        result.put("planNutrition", planNutrition);
        result.put("targetNutrition", targetNutrition);
        result.put("gapReport", gapReport);
        result.put("analysisResult", analysisResult);
        result.put("plans", plans);
        
        return result;
    }
    
    // 计算方案总营养
    private Map<String, BigDecimal> calculatePlanNutrition(List<DailyPlan> plans) {
        Map<String, BigDecimal> total = new HashMap<>();
        total.put("calories", BigDecimal.ZERO);
        total.put("protein", BigDecimal.ZERO);
        total.put("carbs", BigDecimal.ZERO);
        total.put("fat", BigDecimal.ZERO);
        total.put("fiber", BigDecimal.ZERO);
        total.put("calcium", BigDecimal.ZERO);
        total.put("iron", BigDecimal.ZERO);
        total.put("vitc", BigDecimal.ZERO);
        total.put("potassium", BigDecimal.ZERO);
        total.put("sodium", BigDecimal.ZERO);
        
        for (DailyPlan plan : plans) {
            total.put("calories", total.get("calories").add(plan.getCalories() != null ? plan.getCalories() : BigDecimal.ZERO));
            total.put("protein", total.get("protein").add(plan.getProtein() != null ? plan.getProtein() : BigDecimal.ZERO));
            total.put("carbs", total.get("carbs").add(plan.getCarbs() != null ? plan.getCarbs() : BigDecimal.ZERO));
            total.put("fat", total.get("fat").add(plan.getFat() != null ? plan.getFat() : BigDecimal.ZERO));
            // DailyPlan目前只存储基础营养，其他营养需要从Food表获取
            // 这里暂时使用默认值，后续可以扩展
            total.put("fiber", total.get("fiber").add(new BigDecimal("2.5")));
            total.put("calcium", total.get("calcium").add(new BigDecimal("50")));
            total.put("iron", total.get("iron").add(new BigDecimal("1.0")));
            total.put("vitc", total.get("vitc").add(new BigDecimal("10")));
            total.put("potassium", total.get("potassium").add(new BigDecimal("200")));
            total.put("sodium", total.get("sodium").add(new BigDecimal("100")));
        }
        
        return total;
    }
    
    private Map<String, BigDecimal> calculateTotalNutrition(List<MealRecord> records) {
        Map<String, BigDecimal> total = new HashMap<>();
        total.put("calories", BigDecimal.ZERO);
        total.put("protein", BigDecimal.ZERO);
        total.put("carbs", BigDecimal.ZERO);
        total.put("fat", BigDecimal.ZERO);
        total.put("fiber", BigDecimal.ZERO);
        total.put("calcium", BigDecimal.ZERO);
        total.put("iron", BigDecimal.ZERO);
        total.put("vitc", BigDecimal.ZERO);
        total.put("potassium", BigDecimal.ZERO);
        total.put("sodium", BigDecimal.ZERO);
        
        for (MealRecord record : records) {
            total.put("calories", total.get("calories").add(record.getCalories() != null ? record.getCalories() : BigDecimal.ZERO));
            total.put("protein", total.get("protein").add(record.getProtein() != null ? record.getProtein() : BigDecimal.ZERO));
            total.put("carbs", total.get("carbs").add(record.getCarbs() != null ? record.getCarbs() : BigDecimal.ZERO));
            total.put("fat", total.get("fat").add(record.getFat() != null ? record.getFat() : BigDecimal.ZERO));
            total.put("fiber", total.get("fiber").add(record.getFiber() != null ? record.getFiber() : BigDecimal.ZERO));
            total.put("calcium", total.get("calcium").add(record.getCalcium() != null ? record.getCalcium() : BigDecimal.ZERO));
            total.put("iron", total.get("iron").add(record.getIron() != null ? record.getIron() : BigDecimal.ZERO));
            total.put("vitc", total.get("vitc").add(record.getVitc() != null ? record.getVitc() : BigDecimal.ZERO));
            total.put("potassium", total.get("potassium").add(record.getPotassium() != null ? record.getPotassium() : BigDecimal.ZERO));
            total.put("sodium", total.get("sodium").add(record.getSodium() != null ? record.getSodium() : BigDecimal.ZERO));
        }
        
        return total;
    }
    
    private Map<String, BigDecimal> calculateTargetNutrition(User user) {
        Map<String, BigDecimal> target = new HashMap<>();
        
        // 基础目标值
        target.put("calories", user.getDailyCalories());
        target.put("protein", user.getDailyCalories().multiply(new BigDecimal("0.15")).divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP)); // 15%蛋白质
        target.put("carbs", user.getDailyCalories().multiply(new BigDecimal("0.55")).divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP)); // 55%碳水化合物
        target.put("fat", user.getDailyCalories().multiply(new BigDecimal("0.25")).divide(new BigDecimal("9"), 2, RoundingMode.HALF_UP)); // 25%脂肪
        target.put("fiber", new BigDecimal("25"));
        target.put("calcium", new BigDecimal("800"));
        target.put("iron", new BigDecimal("18"));
        target.put("vitc", new BigDecimal("90"));
        target.put("potassium", new BigDecimal("3500"));
        target.put("sodium", new BigDecimal("2300"));
        
        // 根据健康目标调整
        if ("减重".equals(user.getHealthGoal())) {
            target.put("calories", target.get("calories").multiply(new BigDecimal("0.8")));
            target.put("protein", target.get("protein").multiply(new BigDecimal("1.2"))); // 减重需要更多蛋白质
            target.put("fiber", new BigDecimal("30")); // 减重需要更多膳食纤维
        } else if ("增肌".equals(user.getHealthGoal())) {
            target.put("protein", target.get("protein").multiply(new BigDecimal("1.3"))); // 增肌需要更多蛋白质
            target.put("calcium", new BigDecimal("1000")); // 增肌需要更多钙
        } else if ("控制血糖".equals(user.getHealthGoal())) {
            target.put("carbs", target.get("carbs").multiply(new BigDecimal("0.8"))); // 控制血糖需要减少碳水
            target.put("fiber", new BigDecimal("30")); // 控制血糖需要更多膳食纤维
        } else if ("控制血压".equals(user.getHealthGoal())) {
            target.put("sodium", new BigDecimal("1500")); // 控制血压需要减少钠
            target.put("potassium", new BigDecimal("4000")); // 控制血压需要更多钾
        }
        
        return target;
    }
    
    private Map<String, Object> generateGapReport(Map<String, BigDecimal> actual, Map<String, BigDecimal> target) {
        Map<String, Object> gapReport = new HashMap<>();
        
        for (String nutrient : actual.keySet()) {
            BigDecimal actualValue = actual.get(nutrient);
            BigDecimal targetValue = target.get(nutrient);
            BigDecimal gap = actualValue.subtract(targetValue);
            BigDecimal gapPercent = targetValue.compareTo(BigDecimal.ZERO) > 0 ? 
                gap.divide(targetValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO;
            
            Map<String, Object> nutrientInfo = new HashMap<>();
            nutrientInfo.put("actual", actualValue);
            nutrientInfo.put("target", targetValue);
            nutrientInfo.put("gap", gap);
            nutrientInfo.put("gapPercent", gapPercent);
            nutrientInfo.put("status", getNutrientStatus(gapPercent));
            nutrientInfo.put("suggestion", generateNutrientSuggestion(nutrient, gap, gapPercent));
            
            gapReport.put(nutrient, nutrientInfo);
        }
        
        return gapReport;
    }
    
    private String generateNutrientSuggestion(String nutrient, BigDecimal gap, BigDecimal gapPercent) {
        StringBuilder suggestion = new StringBuilder();
        
        if (gap.compareTo(BigDecimal.ZERO) > 0) {
            // 超标情况
            suggestion.append("今日").append(getNutrientName(nutrient)).append("摄入超标，实际摄入：")
                    .append(gap.abs().setScale(1, RoundingMode.HALF_UP)).append("，超出：")
                    .append(gapPercent.abs().setScale(1, RoundingMode.HALF_UP)).append("%，");
            
            switch (nutrient) {
                case "calories":
                    suggestion.append("建议减少高热量食物摄入，增加运动量");
                    break;
                case "protein":
                    suggestion.append("建议适量减少肉类、豆制品等蛋白质含量高的食物");
                    break;
                case "carbs":
                    suggestion.append("建议减少主食、甜食等碳水化合物含量高的食物");
                    break;
                case "fat":
                    suggestion.append("建议减少油炸食品、高脂肪肉类，选择健康脂肪");
                    break;
                case "sodium":
                    suggestion.append("建议减少盐、酱油等调味品的使用，少吃腌制食品和加工肉类");
                    break;
                default:
                    suggestion.append("建议适量调整相关食物摄入");
            }
        } else if (gap.compareTo(BigDecimal.ZERO) < 0) {
            // 不足情况
            suggestion.append("今日").append(getNutrientName(nutrient)).append("摄入不足，实际摄入：")
                    .append(gap.abs().setScale(1, RoundingMode.HALF_UP)).append("，缺口：")
                    .append(gapPercent.abs().setScale(1, RoundingMode.HALF_UP)).append("%，");
            
            switch (nutrient) {
                case "calories":
                    suggestion.append("建议增加食物摄入量，选择营养密度高的食物");
                    break;
                case "protein":
                    suggestion.append("建议增加鸡胸肉、豆腐、鸡蛋等富含蛋白质的食材摄入");
                    break;
                case "carbs":
                    suggestion.append("建议增加全谷物、蔬菜等健康碳水化合物");
                    break;
                case "fat":
                    suggestion.append("建议适量增加健康脂肪，如橄榄油、坚果、鱼类");
                    break;
                case "fiber":
                    suggestion.append("建议增加全谷物、蔬菜、水果等富含膳食纤维的食材");
                    break;
                case "calcium":
                    suggestion.append("建议增加奶制品、豆制品、绿叶蔬菜等富含钙的食材");
                    break;
                case "iron":
                    suggestion.append("建议增加瘦肉、动物肝脏、菠菜等富含铁的食材");
                    break;
                case "vitc":
                    suggestion.append("建议增加柑橘、草莓、西兰花等富含维生素C的蔬菜和水果");
                    break;
                case "potassium":
                    suggestion.append("建议增加香蕉、土豆、菠菜等富含钾的食材");
                    break;
                case "sodium":
                    suggestion.append("建议适量增加盐分摄入，但要注意控制总量");
                    break;
                default:
                    suggestion.append("建议增加相关营养素含量高的食物");
            }
        } else {
            suggestion.append("摄入正常，请保持当前饮食结构");
        }
        
        return suggestion.toString();
    }
    
    private String getNutrientName(String nutrient) {
        switch (nutrient) {
            case "calories": return "热量";
            case "protein": return "蛋白质";
            case "carbs": return "碳水化合物";
            case "fat": return "脂肪";
            case "fiber": return "膳食纤维";
            case "calcium": return "钙";
            case "iron": return "铁";
            case "vitc": return "维生素C";
            case "potassium": return "钾";
            case "sodium": return "钠";
            default: return nutrient;
        }
    }
    
    private String getNutrientStatus(BigDecimal gapPercent) {
        if (gapPercent.compareTo(new BigDecimal("10")) > 0) {
            return "excess"; // 超标
        } else if (gapPercent.compareTo(new BigDecimal("-10")) < 0) {
            return "deficient"; // 不足
        } else {
            return "normal"; // 正常
        }
    }
    
    private String generateAnalysisResult(Map<String, BigDecimal> actual, Map<String, BigDecimal> target, String healthGoal) {
        StringBuilder result = new StringBuilder();
        
        // 总体评价
        int excessCount = 0;
        int deficientCount = 0;
        int normalCount = 0;
        
        for (String nutrient : actual.keySet()) {
            BigDecimal actualValue = actual.get(nutrient);
            BigDecimal targetValue = target.get(nutrient);
            BigDecimal gapPercent = targetValue.compareTo(BigDecimal.ZERO) > 0 ? 
                actualValue.subtract(targetValue).divide(targetValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO;
            
            if (gapPercent.compareTo(new BigDecimal("10")) > 0) {
                excessCount++;
            } else if (gapPercent.compareTo(new BigDecimal("-10")) < 0) {
                deficientCount++;
            } else {
                normalCount++;
            }
        }
        
        // 生成总体评价
        if (excessCount == 0 && deficientCount == 0) {
            result.append("该饮食方案营养均衡，热量适中，各种营养素摄入充足，符合您的").append(healthGoal != null ? healthGoal : "健康").append("目标和每日推荐摄入量。");
        } else if (excessCount > deficientCount) {
            result.append("该饮食方案存在营养摄入超标的问题，");
            if (actual.get("calories").compareTo(target.get("calories").multiply(new BigDecimal("1.2"))) > 0) {
                result.append("热量摄入过多，");
            }
            if (actual.get("fat").compareTo(target.get("fat").multiply(new BigDecimal("1.2"))) > 0) {
                result.append("脂肪摄入过多，");
            }
            if (actual.get("sodium").compareTo(target.get("sodium").multiply(new BigDecimal("1.2"))) > 0) {
                result.append("钠摄入过多，");
            }
            result.append("建议减少油炸食品和高脂肪肉类的摄入，增加蔬菜和水果的摄入。");
        } else if (deficientCount > excessCount) {
            result.append("该饮食方案存在营养摄入不足的问题，");
            if (actual.get("protein").compareTo(target.get("protein").multiply(new BigDecimal("0.8"))) < 0) {
                result.append("蛋白质摄入不足，");
            }
            if (actual.get("fiber").compareTo(target.get("fiber").multiply(new BigDecimal("0.8"))) < 0) {
                result.append("膳食纤维摄入不足，");
            }
            if (actual.get("calcium").compareTo(target.get("calcium").multiply(new BigDecimal("0.8"))) < 0) {
                result.append("钙摄入不足，");
            }
            result.append("建议增加富含这些营养素的食材摄入。");
        } else {
            result.append("该饮食方案营养搭配基本合理，但仍有改进空间，建议根据具体营养素情况进行调整。");
        }
        
        // 根据健康目标给出个性化建议
        result.append(" ");
        switch (healthGoal) {
            case "减重":
                result.append("减重期间建议控制总热量，增加蛋白质和膳食纤维摄入，配合适量有氧运动。");
                break;
            case "增肌":
                result.append("增肌期间建议增加蛋白质摄入，保证充足的热量和钙质，配合力量训练。");
                break;
            case "控制血糖":
                result.append("血糖控制期间建议选择低升糖指数食材，增加膳食纤维摄入，控制碳水化合物总量。");
                break;
            case "控制血压":
                result.append("血压控制期间建议减少钠摄入，增加钾和膳食纤维摄入，控制总热量。");
                break;
            default:
                result.append("建议保持均衡饮食，适量运动，定期监测营养状况。");
        }
        
        return result.toString();
    }
    
    // 获取历史营养分析 - 只返回每个日期最新的记录
    public List<NutritionAnalysis> getNutritionHistory(Long userId, Date startDate, Date endDate) {
        return nutritionAnalysisMapper.findLatestByUserAndDateRange(userId, startDate, endDate);
    }
    
    // 获取统计数据 - 直接从meal_records计算每日统计数据
    public List<Map<String, Object>> getStatistics(Long userId, Date startDate, Date endDate) {
        System.out.println("MealRecordService - getStatistics - userId: " + userId + ", startDate: " + startDate + ", endDate: " + endDate);
        
        List<Map<String, Object>> statistics = new ArrayList<>();
        
        // 获取日期范围内的所有记录
        List<MealRecord> allRecords = mealRecordMapper.findByUserAndDateRange(userId, startDate, endDate);
        System.out.println("查询到的记录数量: " + allRecords.size());
        
        if (allRecords.isEmpty()) {
            return statistics;
        }
        
        // 按日期分组
        Map<Date, List<MealRecord>> recordsByDate = new HashMap<>();
        for (MealRecord record : allRecords) {
            Date date = record.getDate();
            recordsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(record);
        }
        
        // 获取用户信息
        User user = userMapper.findById(userId);
        Map<String, BigDecimal> targetNutrition = calculateTargetNutrition(user);
        
        // 为每个日期计算统计数据
        for (Map.Entry<Date, List<MealRecord>> entry : recordsByDate.entrySet()) {
            Date date = entry.getKey();
            List<MealRecord> dailyRecords = entry.getValue();
            
            // 计算该日期的总营养摄入
            Map<String, BigDecimal> totalNutrition = calculateTotalNutrition(dailyRecords);
            
            // 计算符合率（营养达标比例）
            int complianceCount = 0;
            int totalNutrients = 10;
            
            for (String nutrient : totalNutrition.keySet()) {
                BigDecimal actual = totalNutrition.get(nutrient);
                BigDecimal target = targetNutrition.get(nutrient);
                if (target.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentage = actual.divide(target, 4, RoundingMode.HALF_UP);
                    if (percentage.compareTo(new BigDecimal("0.9")) >= 0 && percentage.compareTo(new BigDecimal("1.1")) <= 0) {
                        complianceCount++;
                    }
                }
            }
            
            int complianceRate = totalNutrients > 0 ? (complianceCount * 100 / totalNutrients) : 0;
            
            // 创建统计对象
            Map<String, Object> dailyStats = new HashMap<>();
            dailyStats.put("date", date);
            dailyStats.put("calories", totalNutrition.get("calories"));
            dailyStats.put("protein", totalNutrition.get("protein"));
            dailyStats.put("carbs", totalNutrition.get("carbs"));
            dailyStats.put("fat", totalNutrition.get("fat"));
            dailyStats.put("fiber", totalNutrition.get("fiber"));
            dailyStats.put("calcium", totalNutrition.get("calcium"));
            dailyStats.put("iron", totalNutrition.get("iron"));
            dailyStats.put("vitc", totalNutrition.get("vitc"));
            dailyStats.put("potassium", totalNutrition.get("potassium"));
            dailyStats.put("sodium", totalNutrition.get("sodium"));
            
            dailyStats.put("targetCalories", targetNutrition.get("calories"));
            dailyStats.put("targetProtein", targetNutrition.get("protein"));
            dailyStats.put("targetCarbs", targetNutrition.get("carbs"));
            dailyStats.put("targetFat", targetNutrition.get("fat"));
            dailyStats.put("targetFiber", targetNutrition.get("fiber"));
            dailyStats.put("targetCalcium", targetNutrition.get("calcium"));
            dailyStats.put("targetIron", targetNutrition.get("iron"));
            dailyStats.put("targetVitc", targetNutrition.get("vitc"));
            dailyStats.put("targetPotassium", targetNutrition.get("potassium"));
            dailyStats.put("targetSodium", targetNutrition.get("sodium"));
            
            dailyStats.put("complianceRate", complianceRate);
            
            statistics.add(dailyStats);
        }
        
        System.out.println("生成的统计数据条数: " + statistics.size());
        return statistics;
    }
    
    // 按周生成营养分析报告
    public Map<String, Object> generateWeeklyReport(Long userId, Date weekStart) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> dailyReports = new ArrayList<>();
        
        // 获取一周的数据
        for (int i = 0; i < 7; i++) {
            Date currentDate = new Date(weekStart.getTime() + i * 24 * 60 * 60 * 1000L);
            try {
                Map<String, Object> dailyAnalysis = analyzeDailyRecords(userId, currentDate);
                dailyReports.add(dailyAnalysis);
            } catch (Exception e) {
                // 如果某天没有数据，跳过
                continue;
            }
        }
        
        if (dailyReports.isEmpty()) {
            throw new RuntimeException("该周没有营养分析数据");
        }
        
        // 计算周平均营养摄入
        Map<String, BigDecimal> weeklyAvg = calculateWeeklyAverage(dailyReports);
        Map<String, BigDecimal> targetNutrition = calculateTargetNutrition(userMapper.findById(userId));
        
        report.put("weekStart", weekStart);
        report.put("dailyReports", dailyReports);
        report.put("weeklyAverage", weeklyAvg);
        report.put("targetNutrition", targetNutrition);
        report.put("weeklyGapReport", generateGapReport(weeklyAvg, targetNutrition));
        report.put("weeklyAnalysis", generateWeeklyAnalysis(weeklyAvg, targetNutrition, dailyReports));
        
        return report;
    }
    
    // 按月生成营养分析报告
    public Map<String, Object> generateMonthlyReport(Long userId, int year, int month) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> weeklyReports = new ArrayList<>();
        
        // 获取月初和月末日期
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(year, month - 1, 1);
        Date monthStart = new Date(cal.getTimeInMillis());
        cal.set(year, month, 0);
        Date monthEnd = new Date(cal.getTimeInMillis());
        
        // 按周生成报告
        Date currentWeekStart = monthStart;
        while (currentWeekStart.before(monthEnd)) {
            try {
                Map<String, Object> weeklyReport = generateWeeklyReport(userId, currentWeekStart);
                weeklyReports.add(weeklyReport);
            } catch (Exception e) {
                // 如果某周没有数据，跳过
            }
            currentWeekStart = new Date(currentWeekStart.getTime() + 7 * 24 * 60 * 60 * 1000L);
        }
        
        if (weeklyReports.isEmpty()) {
            throw new RuntimeException("该月没有营养分析数据");
        }
        
        // 计算月平均营养摄入
        Map<String, BigDecimal> monthlyAvg = calculateMonthlyAverage(weeklyReports);
        Map<String, BigDecimal> targetNutrition = calculateTargetNutrition(userMapper.findById(userId));
        
        report.put("year", year);
        report.put("month", month);
        report.put("weeklyReports", weeklyReports);
        report.put("monthlyAverage", monthlyAvg);
        report.put("targetNutrition", targetNutrition);
        report.put("monthlyGapReport", generateGapReport(monthlyAvg, targetNutrition));
        report.put("monthlyAnalysis", generateMonthlyAnalysis(monthlyAvg, targetNutrition, weeklyReports));
        
        return report;
    }
    
    private Map<String, BigDecimal> calculateWeeklyAverage(List<Map<String, Object>> dailyReports) {
        Map<String, BigDecimal> weeklyAvg = new HashMap<>();
        weeklyAvg.put("calories", BigDecimal.ZERO);
        weeklyAvg.put("protein", BigDecimal.ZERO);
        weeklyAvg.put("carbs", BigDecimal.ZERO);
        weeklyAvg.put("fat", BigDecimal.ZERO);
        weeklyAvg.put("fiber", BigDecimal.ZERO);
        weeklyAvg.put("calcium", BigDecimal.ZERO);
        weeklyAvg.put("iron", BigDecimal.ZERO);
        weeklyAvg.put("vitc", BigDecimal.ZERO);
        weeklyAvg.put("potassium", BigDecimal.ZERO);
        weeklyAvg.put("sodium", BigDecimal.ZERO);
        
        for (Map<String, Object> dailyReport : dailyReports) {
            @SuppressWarnings("unchecked")
            Map<String, BigDecimal> dailyNutrition = (Map<String, BigDecimal>) dailyReport.get("totalNutrition");
            for (String nutrient : weeklyAvg.keySet()) {
                weeklyAvg.put(nutrient, weeklyAvg.get(nutrient).add(dailyNutrition.get(nutrient)));
            }
        }
        
        // 计算平均值
        int dayCount = dailyReports.size();
        for (String nutrient : weeklyAvg.keySet()) {
            weeklyAvg.put(nutrient, weeklyAvg.get(nutrient).divide(new BigDecimal(dayCount), 2, RoundingMode.HALF_UP));
        }
        
        return weeklyAvg;
    }
    
    private Map<String, BigDecimal> calculateMonthlyAverage(List<Map<String, Object>> weeklyReports) {
        Map<String, BigDecimal> monthlyAvg = new HashMap<>();
        monthlyAvg.put("calories", BigDecimal.ZERO);
        monthlyAvg.put("protein", BigDecimal.ZERO);
        monthlyAvg.put("carbs", BigDecimal.ZERO);
        monthlyAvg.put("fat", BigDecimal.ZERO);
        monthlyAvg.put("fiber", BigDecimal.ZERO);
        monthlyAvg.put("calcium", BigDecimal.ZERO);
        monthlyAvg.put("iron", BigDecimal.ZERO);
        monthlyAvg.put("vitc", BigDecimal.ZERO);
        monthlyAvg.put("potassium", BigDecimal.ZERO);
        monthlyAvg.put("sodium", BigDecimal.ZERO);
        
        for (Map<String, Object> weeklyReport : weeklyReports) {
            @SuppressWarnings("unchecked")
            Map<String, BigDecimal> weeklyNutrition = (Map<String, BigDecimal>) weeklyReport.get("weeklyAverage");
            for (String nutrient : monthlyAvg.keySet()) {
                monthlyAvg.put(nutrient, monthlyAvg.get(nutrient).add(weeklyNutrition.get(nutrient)));
            }
        }
        
        // 计算平均值
        int weekCount = weeklyReports.size();
        for (String nutrient : monthlyAvg.keySet()) {
            monthlyAvg.put(nutrient, monthlyAvg.get(nutrient).divide(new BigDecimal(weekCount), 2, RoundingMode.HALF_UP));
        }
        
        return monthlyAvg;
    }
    
    private String generateWeeklyAnalysis(Map<String, BigDecimal> weeklyAvg, Map<String, BigDecimal> target, List<Map<String, Object>> dailyReports) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("本周营养分析：");
        
        // 分析营养趋势
        int goodDays = 0;
        for (Map<String, Object> dailyReport : dailyReports) {
            @SuppressWarnings("unchecked")
            Map<String, Object> gapReport = (Map<String, Object>) dailyReport.get("gapReport");
            int normalCount = 0;
            for (Object nutrientInfo : gapReport.values()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> info = (Map<String, Object>) nutrientInfo;
                if ("normal".equals(info.get("status"))) {
                    normalCount++;
                }
            }
            if (normalCount >= 7) { // 如果7个以上营养素正常
                goodDays++;
            }
        }
        
        analysis.append("本周有").append(goodDays).append("天营养摄入较为均衡。");
        
        // 分析主要问题
        BigDecimal calorieGap = weeklyAvg.get("calories").subtract(target.get("calories"));
        if (calorieGap.compareTo(BigDecimal.ZERO) > 0) {
            analysis.append(" 周平均热量摄入偏高，建议适当控制。");
        } else if (calorieGap.compareTo(BigDecimal.ZERO) < 0) {
            analysis.append(" 周平均热量摄入偏低，建议适当增加。");
        }
        
        return analysis.toString();
    }
    
    // 检查并发送营养提醒
    private void checkAndSendNutritionReminders(Long userId, Map<String, Object> gapReport) {
        for (String nutrient : gapReport.keySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nutrientInfo = (Map<String, Object>) gapReport.get(nutrient);
            String status = (String) nutrientInfo.get("status");
            
            // 如果营养素不足（deficient）或超标（excess），发送提醒
            if ("deficient".equals(status) || "excess".equals(status)) {
                String nutrientName = getNutrientName(nutrient);
                String statusText = "deficient".equals(status) ? "不足" : "超标";
                String suggestion = (String) nutrientInfo.get("suggestion");
                
                // 发送营养提醒
                messageService.sendAutoNutritionReminder(userId, nutrientName, statusText, suggestion);
            }
        }
    }
    
    private String generateMonthlyAnalysis(Map<String, BigDecimal> monthlyAvg, Map<String, BigDecimal> target, List<Map<String, Object>> weeklyReports) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("本月营养分析：");
        
        // 分析营养趋势
        int goodWeeks = 0;
        for (Map<String, Object> weeklyReport : weeklyReports) {
            @SuppressWarnings("unchecked")
            Map<String, Object> gapReport = (Map<String, Object>) weeklyReport.get("weeklyGapReport");
            int normalCount = 0;
            for (Object nutrientInfo : gapReport.values()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> info = (Map<String, Object>) nutrientInfo;
                if ("normal".equals(info.get("status"))) {
                    normalCount++;
                }
            }
            if (normalCount >= 7) { // 如果7个以上营养素正常
                goodWeeks++;
            }
        }
        
        analysis.append("本月有").append(goodWeeks).append("周营养摄入较为均衡。");
        
        // 分析主要问题
        BigDecimal calorieGap = monthlyAvg.get("calories").subtract(target.get("calories"));
        if (calorieGap.compareTo(BigDecimal.ZERO) > 0) {
            analysis.append(" 月平均热量摄入偏高，建议调整饮食结构。");
        } else if (calorieGap.compareTo(BigDecimal.ZERO) < 0) {
            analysis.append(" 月平均热量摄入偏低，建议增加营养密度。");
        }
        
        return analysis.toString();
    }
}


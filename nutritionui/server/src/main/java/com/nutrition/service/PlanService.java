package com.nutrition.service;

import com.nutrition.entity.DailyPlan;
import com.nutrition.entity.Food;
import com.nutrition.entity.User;
import com.nutrition.entity.WeeklyPlan;
import com.nutrition.mapper.DailyPlanMapper;
import com.nutrition.mapper.FoodMapper;
import com.nutrition.mapper.UserMapper;
import com.nutrition.mapper.WeeklyPlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanService {

    @Autowired
    private DailyPlanMapper dailyPlanMapper;

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeeklyPlanMapper weeklyPlanMapper;

    // 生成每日饮食推荐（AI智能分析）
    public List<DailyPlan> generateDailyPlan(Long userId, Date date) {
        // 添加空值检查
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        
        if (date == null) {
            throw new RuntimeException("日期不能为空");
        }
        
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getDailyCalories() == null) {
            throw new RuntimeException("请先完善个人信息，设置每日热量目标");
        }

        // 先删除该日期的旧方案
        dailyPlanMapper.deleteByUserAndDate(userId, date);

        BigDecimal targetCalories = user.getDailyCalories();

        // 根据健康目标调整热量分配
        BigDecimal[] mealCalories = calculateMealCalories(user, targetCalories);

        List<DailyPlan> plans = new ArrayList<>();

        // 生成早餐
        List<DailyPlan> breakfastPlans = generateMeal(userId, date, user, "breakfast", mealCalories[0]);
        if (breakfastPlans.isEmpty()) {
            System.err.println("警告：未能生成早餐方案");
        }
        plans.addAll(breakfastPlans);

        // 生成上午加餐
        DailyPlan morningSnack = generateSnackWithUser(userId, date, user, "morning_snack", mealCalories[4]);
        if (morningSnack != null) {
            plans.add(morningSnack);
        } else {
            System.err.println("警告：未能生成上午加餐方案");
        }

        // 生成午餐
        List<DailyPlan> lunchPlans = generateMeal(userId, date, user, "lunch", mealCalories[1]);
        if (lunchPlans.isEmpty()) {
            System.err.println("警告：未能生成午餐方案");
        }
        plans.addAll(lunchPlans);

        // 生成下午加餐
        DailyPlan afternoonSnack = generateSnackWithUser(userId, date, user, "afternoon_snack", mealCalories[5]);
        if (afternoonSnack != null) {
            plans.add(afternoonSnack);
        } else {
            System.err.println("警告：未能生成下午加餐方案");
        }

        // 生成晚餐
        List<DailyPlan> dinnerPlans = generateMeal(userId, date, user, "dinner", mealCalories[2]);
        if (dinnerPlans.isEmpty()) {
            System.err.println("警告：未能生成晚餐方案");
        }
        plans.addAll(dinnerPlans);

        // 生成晚上加餐
        DailyPlan eveningSnack = generateSnackWithUser(userId, date, user, "evening_snack", mealCalories[3]);
        if (eveningSnack != null) {
            plans.add(eveningSnack);
        } else {
            System.err.println("警告：未能生成晚上加餐方案");
        }

        // 保存到数据库
        for (DailyPlan plan : plans) {
            dailyPlanMapper.insert(plan);
        }
        
        System.out.println("成功生成饮食方案，共" + plans.size() + "项");

        return plans;
    }

    // 根据用户健康目标计算每餐热量
    private BigDecimal[] calculateMealCalories(User user, BigDecimal totalCalories) {
        // [早餐, 午餐, 晚餐, 上午加餐, 下午加餐, 晚上加餐]
        BigDecimal[] calories = new BigDecimal[6];
        String healthGoal = user.getHealthGoal();

        if ("减重".equals(healthGoal)) {
            // 减重目标：早餐25%，午餐40%，晚餐20%，加餐15%
            calories[0] = totalCalories.multiply(new BigDecimal("0.25"));
            calories[1] = totalCalories.multiply(new BigDecimal("0.40"));
            calories[2] = totalCalories.multiply(new BigDecimal("0.20"));
            calories[3] = totalCalories.multiply(new BigDecimal("0.05"));
            calories[4] = totalCalories.multiply(new BigDecimal("0.05"));
            calories[5] = totalCalories.multiply(new BigDecimal("0.05"));
        } else if ("增肌".equals(healthGoal)) {
            // 增肌目标：早餐30%，午餐35%，晚餐30%，加餐5%
            calories[0] = totalCalories.multiply(new BigDecimal("0.30"));
            calories[1] = totalCalories.multiply(new BigDecimal("0.35"));
            calories[2] = totalCalories.multiply(new BigDecimal("0.30"));
            calories[3] = totalCalories.multiply(new BigDecimal("0.05"));
            calories[4] = BigDecimal.ZERO;
            calories[5] = BigDecimal.ZERO;
        } else if ("维持健康".equals(healthGoal)) {
            // 维持健康：早餐25%，午餐35%，晚餐25%，加餐15%
            calories[0] = totalCalories.multiply(new BigDecimal("0.25"));
            calories[1] = totalCalories.multiply(new BigDecimal("0.35"));
            calories[2] = totalCalories.multiply(new BigDecimal("0.25"));
            calories[3] = totalCalories.multiply(new BigDecimal("0.05"));
            calories[4] = totalCalories.multiply(new BigDecimal("0.05"));
            calories[5] = totalCalories.multiply(new BigDecimal("0.05"));
        } else {
            // 默认分配
            calories[0] = totalCalories.multiply(new BigDecimal("0.25"));
            calories[1] = totalCalories.multiply(new BigDecimal("0.35"));
            calories[2] = totalCalories.multiply(new BigDecimal("0.25"));
            calories[3] = totalCalories.multiply(new BigDecimal("0.05"));
            calories[4] = totalCalories.multiply(new BigDecimal("0.05"));
            calories[5] = totalCalories.multiply(new BigDecimal("0.05"));
        }

        return calories;
    }

    private List<DailyPlan> generateMeal(Long userId, Date date, User user, String mealType, BigDecimal targetCalories) {
        List<DailyPlan> plans = new ArrayList<>();
        List<Food> foods = foodMapper.findAll();

        // 根据用户信息和健康目标智能选择食物
        List<Food> selectedFoods = aiSelectFoodsByUser(user, foods, mealType, targetCalories);

        if (selectedFoods.isEmpty()) {
            System.err.println("警告：未能为" + mealType + "选择食物");
            return plans;
        }

        // 根据健康目标调整营养比例
        BigDecimal[] calorieRatios = calculateNutrientRatios(user);

        BigDecimal remainingCalories = targetCalories;

        for (int i = 0; i < selectedFoods.size() && i < calorieRatios.length; i++) {
            Food food = selectedFoods.get(i);
            BigDecimal allocatedCalories = targetCalories.multiply(calorieRatios[i]);

            if (allocatedCalories.compareTo(remainingCalories) > 0) {
                allocatedCalories = remainingCalories;
            }

            if (allocatedCalories.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal amount = allocatedCalories.multiply(new BigDecimal("100"))
                    .divide(food.getCalories(), 2, RoundingMode.HALF_UP);

            // 根据年龄和身体状况调整份量
            amount = adjustAmountByUser(user, food, amount);

            DailyPlan plan = new DailyPlan();
            plan.setUserId(userId);
            plan.setDate(date);
            plan.setMealType(mealType);
            plan.setFoodId(food.getId());
            plan.setFoodName(food.getName());
            plan.setAmount(amount);
            plan.setCalories(food.getCalories().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            plan.setProtein(food.getProtein().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            plan.setCarbs(food.getCarbs().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            plan.setFat(food.getFat().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

            plans.add(plan);
            remainingCalories = remainingCalories.subtract(plan.getCalories());
        }

        return plans;
    }

    private DailyPlan generateSnackWithUser(Long userId, Date date, User user, String mealType, BigDecimal targetCalories) {
        List<Food> snacks = new ArrayList<>();

        // 根据健康目标选择适合的加餐
        if ("减重".equals(user.getHealthGoal())) {
            // 减重选择低热量、高纤维的食物
            snacks.addAll(foodMapper.findByCategory("水果"));
            snacks.addAll(foodMapper.findByCategory("蔬菜"));
            snacks.addAll(foodMapper.findByCategory("坚果"));
        } else if ("增肌".equals(user.getHealthGoal())) {
            // 增肌选择高蛋白、优质碳水的食物
            snacks.addAll(foodMapper.findByCategory("奶制品"));
            snacks.addAll(foodMapper.findByCategory("蛋白质"));
            snacks.addAll(foodMapper.findByCategory("水果"));
        } else {
            // 维持健康选择均衡的食物
            snacks.addAll(foodMapper.findByCategory("水果"));
            snacks.addAll(foodMapper.findByCategory("奶制品"));
            snacks.addAll(foodMapper.findByCategory("坚果"));
        }

        if (snacks.isEmpty()) {
            snacks = foodMapper.findAll();
        }

        // 根据年龄和性别过滤不合适的食物
        snacks = filterFoodsByUser(user, snacks);

        if (snacks.isEmpty()) {
            snacks = foodMapper.findAll();
        }

        // 如果仍然没有食物可选，返回null
        if (snacks.isEmpty()) {
            System.err.println("错误：没有任何可用的加餐食物");
            return null;
        }

        Collections.shuffle(snacks);
        Food food = snacks.get(0);

        BigDecimal amount = targetCalories.multiply(new BigDecimal("100"))
                .divide(food.getCalories(), 2, RoundingMode.HALF_UP);

        // 根据用户信息调整份量
        amount = adjustAmountByUser(user, food, amount);

        DailyPlan plan = new DailyPlan();
        plan.setUserId(userId);
        plan.setDate(date);
        plan.setMealType(mealType);
        plan.setFoodId(food.getId());
        plan.setFoodName(food.getName());
        plan.setAmount(amount);
        plan.setCalories(food.getCalories().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setProtein(food.getProtein().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setCarbs(food.getCarbs().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setFat(food.getFat().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

        return plan;
    }

    // AI智能选择食物（基于用户信息和健康目标）
    private List<Food> aiSelectFoodsByUser(User user, List<Food> foods, String mealType, BigDecimal targetCalories) {
        List<Food> selected = new ArrayList<>();
        Random random = new Random();

        // 获取用户健康目标
        String healthGoal = user.getHealthGoal();
        Integer age = user.getAge();
        Integer gender = user.getGender(); // 0:女, 1:男

        // 谷物（碳水化合物）选择
        List<Food> grains = foods.stream().filter(f -> "谷物".equals(f.getCategory())).collect(Collectors.toList());
        if (!grains.isEmpty()) {
            // 根据健康目标选择不同GI值的谷物
            List<Food> filteredGrains = grains;
            if ("减重".equals(healthGoal)) {
                // 减重选择低GI谷物
                filteredGrains = grains.stream().filter(f -> f.getCalories().compareTo(new BigDecimal("350")) < 0).collect(Collectors.toList());
                if (filteredGrains.isEmpty()) filteredGrains = grains;
            } else if ("增肌".equals(healthGoal)) {
                // 增肌选择高GI谷物
                filteredGrains = grains.stream().filter(f -> f.getCalories().compareTo(new BigDecimal("300")) > 0).collect(Collectors.toList());
                if (filteredGrains.isEmpty()) filteredGrains = grains;
            }
            selected.add(filteredGrains.get(random.nextInt(filteredGrains.size())));
        } else {
            System.err.println("警告：没有找到谷物类食物");
        }

        // 蛋白质食物选择
        List<Food> proteins = foods.stream().filter(f ->
                "蛋白质".equals(f.getCategory()) || "肉类".equals(f.getCategory()) ||
                        "蛋类".equals(f.getCategory()) || "豆类".equals(f.getCategory())).collect(Collectors.toList());
        if (!proteins.isEmpty()) {
            // 根据健康目标和年龄选择蛋白质
            List<Food> filteredProteins = proteins;
            if ("减重".equals(healthGoal)) {
                // 减重选择低脂肪高蛋白食物
                filteredProteins = proteins.stream().filter(f -> f.getFat().compareTo(new BigDecimal("5")) < 0).collect(Collectors.toList());
                if (filteredProteins.isEmpty()) filteredProteins = proteins;
            } else if ("增肌".equals(healthGoal)) {
                // 增肌选择高蛋白食物
                filteredProteins = proteins.stream().filter(f -> f.getProtein().compareTo(new BigDecimal("20")) > 0).collect(Collectors.toList());
                if (filteredProteins.isEmpty()) filteredProteins = proteins;
            }

            // 年龄大于50岁，增加豆类摄入
            if (age != null && age > 50) {
                List<Food> beans = filteredProteins.stream().filter(f -> "豆类".equals(f.getCategory())).collect(Collectors.toList());
                if (!beans.isEmpty()) {
                    selected.add(beans.get(random.nextInt(beans.size())));
                } else {
                    selected.add(filteredProteins.get(random.nextInt(filteredProteins.size())));
                }
            } else {
                selected.add(filteredProteins.get(random.nextInt(filteredProteins.size())));
            }
        } else {
            System.err.println("警告：没有找到蛋白质类食物");
        }

        // 蔬菜选择
        List<Food> vegetables = foods.stream().filter(f -> "蔬菜".equals(f.getCategory())).collect(Collectors.toList());
        if (!vegetables.isEmpty()) {
            // 根据健康目标选择蔬菜
            List<Food> filteredVegetables = vegetables;
            if ("减重".equals(healthGoal)) {
                // 减重选择高纤维、低热量蔬菜
                filteredVegetables = vegetables.stream().filter(f -> f.getCalories().compareTo(new BigDecimal("30")) < 0).collect(Collectors.toList());
                if (filteredVegetables.isEmpty()) filteredVegetables = vegetables;
            }
            selected.add(filteredVegetables.get(random.nextInt(filteredVegetables.size())));
        } else {
            System.err.println("警告：没有找到蔬菜类食物");
        }

        // 健康脂肪选择
        if (targetCalories.compareTo(new BigDecimal("400")) > 0) {
            List<Food> fats = foods.stream().filter(f ->
                    "坚果".equals(f.getCategory()) || "油脂".equals(f.getCategory())).collect(Collectors.toList());
            if (!fats.isEmpty()) {
                // 根据健康目标选择脂肪
                List<Food> filteredFats = fats;
                if ("减重".equals(healthGoal)) {
                    // 减重选择健康脂肪，控制摄入量
                    filteredFats = fats.stream().filter(f -> f.getCalories().compareTo(new BigDecimal("600")) < 0).collect(Collectors.toList());
                    if (filteredFats.isEmpty()) filteredFats = fats;
                }
                selected.add(filteredFats.get(random.nextInt(filteredFats.size())));
            } else {
                System.err.println("警告：没有找到脂肪类食物");
            }
        }

        if (selected.isEmpty()) {
            System.err.println("严重警告：未能选择任何食物");
        }

        return selected;
    }

    // 根据用户信息调整份量
    private BigDecimal adjustAmountByUser(User user, Food food, BigDecimal amount) {
        Integer age = user.getAge();
        Integer gender = user.getGender(); // 0:女, 1:男
        String healthGoal = user.getHealthGoal();

        // 年龄调整
        if (age != null) {
            if (age < 18 || age > 60) {
                // 青少年和老年人减少份量
                amount = amount.multiply(new BigDecimal("0.8"));
            }
        }

        // 性别调整
        if (gender != null && gender == 1) {
            // 男性增加份量
            amount = amount.multiply(new BigDecimal("1.2"));
        }

        // 健康目标调整
        if ("减重".equals(healthGoal)) {
            // 减重减少份量
            amount = amount.multiply(new BigDecimal("0.9"));
        } else if ("增肌".equals(healthGoal)) {
            // 增肌增加份量
            amount = amount.multiply(new BigDecimal("1.1"));
        }

        // 确保份量在合理范围内
        if (amount.compareTo(new BigDecimal("30")) < 0) {
            amount = new BigDecimal("30");
        } else if (amount.compareTo(new BigDecimal("400")) > 0) {
            amount = new BigDecimal("400");
        }

        return amount;
    }

    // 根据用户信息过滤不合适的食物
    private List<Food> filterFoodsByUser(User user, List<Food> foods) {
        Integer age = user.getAge();
        Integer gender = user.getGender();

        List<Food> filteredFoods = new ArrayList<>();

        for (Food food : foods) {
            boolean suitable = true;

            // 年龄限制
            if (age != null) {
                if (age < 12 && "坚果".equals(food.getCategory())) {
                    // 儿童避免坚果（窒息风险）
                    suitable = false;
                } else if (age > 60 && "肉类".equals(food.getCategory()) && food.getFat().compareTo(new BigDecimal("15")) > 0) {
                    // 老年人避免高脂肪肉类
                    suitable = false;
                }
            }

            if (suitable) {
                filteredFoods.add(food);
            }
        }

        return filteredFoods;
    }

    // 根据健康目标计算营养比例
    private BigDecimal[] calculateNutrientRatios(User user) {
        String healthGoal = user.getHealthGoal();

        if ("减重".equals(healthGoal)) {
            // 减重：低热量，高纤维，适量蛋白质
            return new BigDecimal[]{
                    new BigDecimal("0.3"), // 谷物 30%
                    new BigDecimal("0.4"), // 蛋白质 40%
                    new BigDecimal("0.25"), // 蔬菜 25%
                    new BigDecimal("0.05")  // 脂肪 5%
            };
        } else if ("增肌".equals(healthGoal)) {
            // 增肌：高蛋白质，适量碳水，健康脂肪
            return new BigDecimal[]{
                    new BigDecimal("0.45"), // 谷物 45%
                    new BigDecimal("0.4"), // 蛋白质 40%
                    new BigDecimal("0.1"), // 蔬菜 10%
                    new BigDecimal("0.05")  // 脂肪 5%
            };
        } else {
            // 维持健康：均衡比例
            return new BigDecimal[]{
                    new BigDecimal("0.4"), // 谷物 40%
                    new BigDecimal("0.3"), // 蛋白质 30%
                    new BigDecimal("0.2"), // 蔬菜 20%
                    new BigDecimal("0.1")  // 脂肪 10%
            };
        }
    }

    public List<DailyPlan> getDailyPlan(Long userId, Date date) {
        return dailyPlanMapper.findByUserAndDate(userId, date);
    }

    // 生成每周饮食方案
    public Map<String, Object> generateWeeklyPlan(Long userId, Date weekStart) {
        User user = userMapper.findById(userId);
        if (user == null || user.getDailyCalories() == null) {
            throw new RuntimeException("请先完善个人信息");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> weeklyPlans = new ArrayList<>();

        // 生成7天的方案
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = weekStart.toLocalDate().plusDays(i);
            Date date = Date.valueOf(currentDate);

            // 删除当天已有方案
            dailyPlanMapper.deleteByUserAndDate(userId, date);

            // 生成新方案
            List<DailyPlan> dailyPlans = generateDailyPlan(userId, date);

            Map<String, Object> dayPlan = new HashMap<>();
            dayPlan.put("date", date);
            dayPlan.put("plans", dailyPlans);
            weeklyPlans.add(dayPlan);
        }

        // 保存为每周方案
        WeeklyPlan weeklyPlan = new WeeklyPlan();
        weeklyPlan.setUserId(userId);
        weeklyPlan.setWeekStart(weekStart);
        weeklyPlan.setIsTemplate(false);
        weeklyPlanMapper.insert(weeklyPlan);

        result.put("weeklyPlan", weeklyPlan);
        result.put("dailyPlans", weeklyPlans);

        return result;
    }

    // 获取每周饮食方案
    public Map<String, Object> getWeeklyPlan(Long userId, Date weekStart) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> weeklyPlans = new ArrayList<>();

        // 获取7天的方案
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = weekStart.toLocalDate().plusDays(i);
            Date date = Date.valueOf(currentDate);

            List<DailyPlan> dailyPlans = dailyPlanMapper.findByUserAndDate(userId, date);

            Map<String, Object> dayPlan = new HashMap<>();
            dayPlan.put("date", currentDate.toString());
            dayPlan.put("plans", dailyPlans);

            weeklyPlans.add(dayPlan);
        }

        // 查找周方案记录
        WeeklyPlan weeklyPlan = weeklyPlanMapper.findByUserAndWeek(userId, weekStart);

        result.put("weeklyPlan", weeklyPlan);
        result.put("dailyPlans", weeklyPlans);

        return result;
    }

    // 保存每周方案为模板
    public void saveWeeklyTemplate(Long userId, Date weekStart, String templateName) {
        WeeklyPlan template = new WeeklyPlan();
        template.setUserId(userId);
        template.setWeekStart(weekStart);
        template.setIsTemplate(true);
        template.setTemplateName(templateName);
        weeklyPlanMapper.insert(template);
    }

    // 应用模板
    public Map<String, Object> applyTemplate(Long userId, Long templateId, Date weekStart) {
        WeeklyPlan template = weeklyPlanMapper.findById(templateId);
        if (template == null || !template.getIsTemplate()) {
            throw new RuntimeException("模板不存在");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> weeklyPlans = new ArrayList<>();

        // 复制模板中的方案到新的一周
        for (int i = 0; i < 7; i++) {
            LocalDate templateDate = template.getWeekStart().toLocalDate().plusDays(i);
            LocalDate newDate = weekStart.toLocalDate().plusDays(i);

            List<DailyPlan> templatePlans = dailyPlanMapper.findByUserAndDate(userId, Date.valueOf(templateDate));

            // 删除新日期已有方案
            dailyPlanMapper.deleteByUserAndDate(userId, Date.valueOf(newDate));

            // 复制方案
            List<DailyPlan> newPlans = new ArrayList<>();
            for (DailyPlan templatePlan : templatePlans) {
                DailyPlan newPlan = new DailyPlan();
                newPlan.setUserId(userId);
                newPlan.setDate(Date.valueOf(newDate));
                newPlan.setMealType(templatePlan.getMealType());
                newPlan.setFoodId(templatePlan.getFoodId());
                newPlan.setFoodName(templatePlan.getFoodName());
                newPlan.setAmount(templatePlan.getAmount());
                newPlan.setCalories(templatePlan.getCalories());
                newPlan.setProtein(templatePlan.getProtein());
                newPlan.setCarbs(templatePlan.getCarbs());
                newPlan.setFat(templatePlan.getFat());

                dailyPlanMapper.insert(newPlan);
                newPlans.add(newPlan);
            }

            Map<String, Object> dayPlan = new HashMap<>();
            dayPlan.put("date", Date.valueOf(newDate));
            dayPlan.put("plans", newPlans);
            weeklyPlans.add(dayPlan);
        }

        result.put("dailyPlans", weeklyPlans);
        return result;
    }

    // 替换食材
    public Map<String, Object> replaceFood(Long userId, Long planId, Long newFoodId, BigDecimal newAmount, String date) {
        DailyPlan plan = dailyPlanMapper.findById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("方案不存在");
        }

        Food newFood = foodMapper.findById(newFoodId);
        if (newFood == null) {
            throw new RuntimeException("食材不存在");
        }

        // 计算营养成分变化
        Map<String, Object> nutritionChange = calculateNutritionChange(plan, newFood, newAmount);

        // 先进行DRI符合性检查（在替换之前）
        Map<String, Object> driCheck = checkDRICompliance(userId, nutritionChange);

        // 如果不符合DRI标准，仍然允许替换但给出警告
        // 这里不阻止替换，只是给出建议

        // 更新方案
        plan.setFoodId(newFoodId);
        plan.setFoodName(newFood.getName());
        plan.setAmount(newAmount);
        plan.setCalories(newFood.getCalories().multiply(newAmount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setProtein(newFood.getProtein().multiply(newAmount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setCarbs(newFood.getCarbs().multiply(newAmount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setFat(newFood.getFat().multiply(newAmount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

        dailyPlanMapper.update(plan);

        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);
        result.put("nutritionChange", nutritionChange);
        result.put("driCheck", driCheck);

        return result;
    }

    // 计算营养成分变化
    private Map<String, Object> calculateNutritionChange(DailyPlan originalPlan, Food newFood, BigDecimal newAmount) {
        Map<String, Object> change = new HashMap<>();

        // 获取原食材信息
        Food originalFood = foodMapper.findById(originalPlan.getFoodId());
        if (originalFood == null) {
            return change;
        }

        // 计算原营养成分（每100g）
        BigDecimal originalCalories = originalFood.getCalories();
        BigDecimal originalProtein = originalFood.getProtein();
        BigDecimal originalCarbs = originalFood.getCarbs();
        BigDecimal originalFat = originalFood.getFat();
        BigDecimal originalFiber = originalFood.getFiber() != null ? originalFood.getFiber() : BigDecimal.ZERO;
        BigDecimal originalCalcium = originalFood.getCalcium() != null ? originalFood.getCalcium() : BigDecimal.ZERO;
        BigDecimal originalIron = originalFood.getIron() != null ? originalFood.getIron() : BigDecimal.ZERO;
        BigDecimal originalVitC = originalFood.getVitc() != null ? originalFood.getVitc() : BigDecimal.ZERO;

        // 计算新营养成分（每100g）
        BigDecimal newCalories = newFood.getCalories();
        BigDecimal newProtein = newFood.getProtein();
        BigDecimal newCarbs = newFood.getCarbs();
        BigDecimal newFat = newFood.getFat();
        BigDecimal newFiber = newFood.getFiber() != null ? newFood.getFiber() : BigDecimal.ZERO;
        BigDecimal newCalcium = newFood.getCalcium() != null ? newFood.getCalcium() : BigDecimal.ZERO;
        BigDecimal newIron = newFood.getIron() != null ? newFood.getIron() : BigDecimal.ZERO;
        BigDecimal newVitC = newFood.getVitc() != null ? newFood.getVitc() : BigDecimal.ZERO;

        // 计算变化量（基于新重量）
        BigDecimal amountRatio = newAmount.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        change.put("caloriesChange", newCalories.subtract(originalCalories).multiply(amountRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());
        change.put("proteinChange", newProtein.subtract(originalProtein).multiply(amountRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());
        change.put("carbsChange", newCarbs.subtract(originalCarbs).multiply(amountRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());
        change.put("fatChange", newFat.subtract(originalFat).multiply(amountRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());
        change.put("fiberChange", newFiber.subtract(originalFiber).multiply(amountRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());
        change.put("calciumChange", newCalcium.subtract(originalCalcium).multiply(amountRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());
        change.put("ironChange", newIron.subtract(originalIron).multiply(amountRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());
        change.put("vitCChange", newVitC.subtract(originalVitC).multiply(amountRatio).setScale(2, RoundingMode.HALF_UP).doubleValue());

        return change;
    }

    // DRI符合性检查
    public Map<String, Object> checkDRICompliance(Long userId, Map<String, Object> nutritionChange) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // 获取用户每日目标
        BigDecimal dailyCalories = user.getDailyCalories();
        String healthGoal = user.getHealthGoal();

        // 根据健康目标调整DRI标准
        BigDecimal dailyProtein, dailyCarbs, dailyFat, driFiber, driCalcium;

        // 基础标准
        dailyProtein = dailyCalories.multiply(new BigDecimal("0.15")).divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP); // 15%蛋白质
        dailyCarbs = dailyCalories.multiply(new BigDecimal("0.55")).divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP); // 55%碳水化合物
        dailyFat = dailyCalories.multiply(new BigDecimal("0.25")).divide(new BigDecimal("9"), 2, RoundingMode.HALF_UP); // 25%脂肪
        driFiber = new BigDecimal("25"); // 25g膳食纤维
        driCalcium = new BigDecimal("800"); // 800mg钙

        // 根据健康目标调整标准
        if ("减重".equals(healthGoal)) {
            driFiber = new BigDecimal("30"); // 减重需要更多膳食纤维
            dailyProtein = dailyCalories.multiply(new BigDecimal("0.20")).divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP); // 减重需要更多蛋白质
        } else if ("增肌".equals(healthGoal)) {
            dailyProtein = dailyCalories.multiply(new BigDecimal("0.25")).divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP); // 增肌需要更多蛋白质
            driCalcium = new BigDecimal("1000"); // 增肌需要更多钙
        } else if ("控制血糖".equals(healthGoal)) {
            dailyCarbs = dailyCalories.multiply(new BigDecimal("0.45")).divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP); // 控制血糖需要减少碳水
            driFiber = new BigDecimal("30"); // 控制血糖需要更多膳食纤维
        }

        final BigDecimal finalDailyProtein = dailyProtein;
        final BigDecimal finalDailyCarbs = dailyCarbs;
        final BigDecimal finalDriFiber = driFiber;
        final BigDecimal finalDriCalcium = driCalcium;

        BigDecimal driIron = new BigDecimal("18"); // 18mg铁（女性）
        BigDecimal driVitC = new BigDecimal("90"); // 90mg维生素C

        // 获取当前日期的总营养摄入
        Date today = new Date(System.currentTimeMillis());
        List<DailyPlan> todayPlans = dailyPlanMapper.findByUserAndDate(userId, today);

        // 计算当前总营养摄入
        BigDecimal currentTotalCalories = BigDecimal.ZERO;
        BigDecimal currentTotalProtein = BigDecimal.ZERO;
        BigDecimal currentTotalCarbs = BigDecimal.ZERO;
        BigDecimal currentTotalFat = BigDecimal.ZERO;

        for (DailyPlan plan : todayPlans) {
            currentTotalCalories = currentTotalCalories.add(plan.getCalories());
            currentTotalProtein = currentTotalProtein.add(plan.getProtein());
            currentTotalCarbs = currentTotalCarbs.add(plan.getCarbs());
            currentTotalFat = currentTotalFat.add(plan.getFat());
        }

        // 获取营养变化数据
        Double caloriesChange = (Double) nutritionChange.get("caloriesChange");
        Double proteinChange = (Double) nutritionChange.get("proteinChange");
        Double carbsChange = (Double) nutritionChange.get("carbsChange");
        Double fatChange = (Double) nutritionChange.get("fatChange");

        // 计算替换后的总营养摄入
        BigDecimal newTotalCalories = currentTotalCalories.add(new BigDecimal(caloriesChange != null ? caloriesChange : 0));
        BigDecimal newTotalProtein = currentTotalProtein.add(new BigDecimal(proteinChange != null ? proteinChange : 0));
        BigDecimal newTotalCarbs = currentTotalCarbs.add(new BigDecimal(carbsChange != null ? carbsChange : 0));
        BigDecimal newTotalFat = currentTotalFat.add(new BigDecimal(fatChange != null ? fatChange : 0));

        // 检查热量
        if (caloriesChange != null) {
            // 检查绝对数值是否合理
            if (newTotalCalories.compareTo(dailyCalories.multiply(new BigDecimal("1.5"))) > 0) {
                warnings.add("总热量摄入过多，超过每日推荐量的150%");
                suggestions.add("建议减少食材重量或选择低热量替代品");
            } else if (newTotalCalories.compareTo(dailyCalories.multiply(new BigDecimal("0.5"))) < 0) {
                warnings.add("总热量摄入不足，低于每日推荐量的50%");
                suggestions.add("建议增加食材重量或选择高热量替代品");
            } else if (Math.abs(caloriesChange) > dailyCalories.multiply(new BigDecimal("0.15")).doubleValue()) {
                if (caloriesChange > 0) {
                    warnings.add("热量增加过多，可能影响" + (healthGoal != null ? healthGoal : "健康") + "目标");
                    suggestions.add("建议减少食材重量或选择低热量替代品");
                } else {
                    warnings.add("热量减少过多，可能无法满足日常需求");
                    suggestions.add("建议增加食材重量或选择高热量替代品");
                }
            }
        }

        // 检查蛋白质
        if (proteinChange != null) {
            // 检查绝对数值是否合理
            if (newTotalProtein.compareTo(finalDailyProtein.multiply(new BigDecimal("2.0"))) > 0) {
                warnings.add("总蛋白质摄入过多，超过每日推荐量的200%");
                suggestions.add("建议减少蛋白质含量高的食材重量");
            } else if (newTotalProtein.compareTo(finalDailyProtein.multiply(new BigDecimal("0.3"))) < 0) {
                warnings.add("总蛋白质摄入不足，低于每日推荐量的30%");
                suggestions.add("建议增加蛋白质含量高的食材，如瘦肉、豆制品、蛋类");
            } else if (Math.abs(proteinChange) > finalDailyProtein.multiply(new BigDecimal("0.25")).doubleValue()) {
                if (proteinChange < 0) {
                    warnings.add("蛋白质摄入减少，可能影响" + (healthGoal != null ? healthGoal : "健康") + "目标");
                    suggestions.add("建议增加蛋白质含量高的食材，如瘦肉、豆制品、蛋类");
                } else {
                    if ("减重".equals(healthGoal) || "增肌".equals(healthGoal)) {
                        // 减重和增肌需要更多蛋白质，增加是好事
                        suggestions.add("蛋白质增加有助于" + healthGoal + "目标");
                    } else {
                        warnings.add("蛋白质摄入过多，可能增加肾脏负担");
                        suggestions.add("建议适量减少蛋白质摄入");
                    }
                }
            }
        }

        // 检查碳水化合物
        if (carbsChange != null) {
            // 检查绝对数值是否合理
            if (newTotalCarbs.compareTo(finalDailyCarbs.multiply(new BigDecimal("2.0"))) > 0) {
                warnings.add("总碳水化合物摄入过多，超过每日推荐量的200%");
                suggestions.add("建议减少碳水化合物含量高的食材重量");
            } else if (newTotalCarbs.compareTo(finalDailyCarbs.multiply(new BigDecimal("0.3"))) < 0) {
                warnings.add("总碳水化合物摄入不足，低于每日推荐量的30%");
                suggestions.add("建议增加全谷物、蔬菜等健康碳水化合物");
            } else if (Math.abs(carbsChange) > finalDailyCarbs.multiply(new BigDecimal("0.2")).doubleValue()) {
                if ("控制血糖".equals(healthGoal)) {
                    if (carbsChange > 0) {
                        warnings.add("碳水化合物增加过多，不利于血糖控制");
                        suggestions.add("建议选择低升糖指数食材，如燕麦、糙米、蔬菜");
                    }
                } else if (carbsChange < 0) {
                    warnings.add("碳水化合物摄入不足，可能影响能量供应");
                    suggestions.add("建议增加全谷物、蔬菜等健康碳水化合物");
                }
            }
        }

        // 检查脂肪
        if (fatChange != null) {
            // 检查绝对数值是否合理
            if (newTotalFat.compareTo(dailyFat.multiply(new BigDecimal("2.0"))) > 0) {
                warnings.add("总脂肪摄入过多，超过每日推荐量的200%");
                suggestions.add("建议减少脂肪含量高的食材重量");
            } else if (newTotalFat.compareTo(dailyFat.multiply(new BigDecimal("0.3"))) < 0) {
                warnings.add("总脂肪摄入不足，低于每日推荐量的30%");
                suggestions.add("建议适量增加健康脂肪摄入");
            } else if (Math.abs(fatChange) > dailyFat.multiply(new BigDecimal("0.3")).doubleValue()) {
                if (fatChange > 0) {
                    warnings.add("脂肪摄入增加过多，可能影响" + (healthGoal != null ? healthGoal : "健康") + "目标");
                    suggestions.add("建议选择健康脂肪，如橄榄油、坚果、鱼类");
                } else {
                    warnings.add("脂肪摄入不足，可能影响脂溶性维生素吸收");
                    suggestions.add("建议适量增加健康脂肪摄入");
                }
            }
        }

        // 检查维生素C
        Double vitCChange = (Double) nutritionChange.get("vitCChange");
        if (vitCChange != null) {
            if (vitCChange < -driVitC.multiply(new BigDecimal("0.15")).doubleValue()) {
                warnings.add("维生素C摄入可能不足，影响免疫力");
                suggestions.add("建议增加富含维生素C的蔬菜和水果，如柑橘、草莓、西兰花");
            }
        }

        // 检查钙
        Double calciumChange = (Double) nutritionChange.get("calciumChange");
        if (calciumChange != null) {
            if (calciumChange < -driCalcium.multiply(new BigDecimal("0.15")).doubleValue()) {
                warnings.add("钙摄入可能不足，影响骨骼健康");
                suggestions.add("建议增加奶制品、豆制品、绿叶蔬菜等富含钙的食材");
            }
        }

        // 检查铁
        Double ironChange = (Double) nutritionChange.get("ironChange");
        if (ironChange != null) {
            if (ironChange < -driIron.multiply(new BigDecimal("0.2")).doubleValue()) {
                warnings.add("铁摄入可能不足，可能导致贫血");
                suggestions.add("建议增加瘦肉、动物肝脏、菠菜等富含铁的食材");
            }
        }

        // 检查膳食纤维
        Double fiberChange = (Double) nutritionChange.get("fiberChange");
        if (fiberChange != null) {
            if (fiberChange < -finalDriFiber.multiply(new BigDecimal("0.2")).doubleValue()) {
                warnings.add("膳食纤维摄入可能不足，影响消化健康");
                suggestions.add("建议增加全谷物、蔬菜、水果等富含膳食纤维的食材");
            }
        }

        // 综合评估
        boolean isCompliant = warnings.isEmpty();
        if (!isCompliant) {
            // 根据健康目标给出针对性建议
            if ("减重".equals(healthGoal)) {
                suggestions.add("减重期间建议控制总热量，增加蛋白质和膳食纤维摄入");
            } else if ("增肌".equals(healthGoal)) {
                suggestions.add("增肌期间建议增加蛋白质摄入，保证充足的热量和钙质");
            } else if ("控制血糖".equals(healthGoal)) {
                suggestions.add("血糖控制期间建议选择低升糖指数食材，增加膳食纤维摄入");
            } else if ("控制血压".equals(healthGoal)) {
                suggestions.add("血压控制期间建议减少钠摄入，增加钾和膳食纤维摄入");
            }
        }

        result.put("isCompliant", isCompliant);
        result.put("warnings", warnings);
        result.put("suggestions", suggestions);
        result.put("healthGoal", healthGoal);
        result.put("driStandards", new HashMap<String, Object>() {{
            put("dailyCalories", dailyCalories);
            put("dailyProtein", finalDailyProtein);
            put("dailyCarbs", finalDailyCarbs);
            put("dailyFat", dailyFat);
            put("driFiber", finalDriFiber);
            put("driCalcium", finalDriCalcium);
            put("driIron", driIron);
            put("driVitC", driVitC);
        }});

        return result;
    }

    // 获取相似食材推荐
    public List<Map<String, Object>> getSimilarFoods(Long foodId, int limit) {
        Food originalFood = foodMapper.findById(foodId);
        if (originalFood == null) {
            return new ArrayList<>();
        }

        List<Food> allFoods = foodMapper.findAll();
        List<Map<String, Object>> similarFoods = new ArrayList<>();

        for (Food food : allFoods) {
            if (food.getId().equals(foodId)) continue; // 排除自己

            double similarity = calculateNutritionSimilarity(originalFood, food);
            if (similarity > 0.6) { // 相似度阈值
                Map<String, Object> similarFood = new HashMap<>();
                similarFood.put("food", food);
                similarFood.put("similarity", similarity);
                similarFoods.add(similarFood);
            }
        }

        // 按相似度排序
        similarFoods.sort((a, b) -> Double.compare((Double) b.get("similarity"), (Double) a.get("similarity")));

        return similarFoods.stream().limit(limit).collect(Collectors.toList());
    }

    // 计算营养成分相似度
    private double calculateNutritionSimilarity(Food food1, Food food2) {
        // 使用欧几里得距离计算相似度
        double caloriesDiff = Math.pow(food1.getCalories().doubleValue() - food2.getCalories().doubleValue(), 2);
        double proteinDiff = Math.pow(food1.getProtein().doubleValue() - food2.getProtein().doubleValue(), 2);
        double carbsDiff = Math.pow(food1.getCarbs().doubleValue() - food2.getCarbs().doubleValue(), 2);
        double fatDiff = Math.pow(food1.getFat().doubleValue() - food2.getFat().doubleValue(), 2);

        // 归一化处理
        double distance = Math.sqrt(caloriesDiff / 10000 + proteinDiff / 100 + carbsDiff / 100 + fatDiff / 100);

        // 转换为相似度（0-1之间）
        return Math.max(0, 1 - distance / 10);
    }

    // 重新生成方案（根据用户需求）
    public List<DailyPlan> regeneratePlan(Long userId, Date date, String requirements) {
        // 删除当天已有方案
        dailyPlanMapper.deleteByUserAndDate(userId, date);

        User user = userMapper.findById(userId);
        BigDecimal targetCalories = user.getDailyCalories();

        // 根据需求调整
        if (requirements.contains("不想吃主食")) {
            targetCalories = targetCalories.multiply(new BigDecimal("0.8"));
        } else if (requirements.contains("不想吃肉类")) {
            targetCalories = targetCalories.multiply(new BigDecimal("0.9"));
        }

        // 重新生成方案
        return generateDailyPlanWithRequirements(userId, date, targetCalories, requirements);
    }

    private List<DailyPlan> generateDailyPlanWithRequirements(Long userId, Date date, BigDecimal targetCalories, String requirements) {
        List<DailyPlan> plans = new ArrayList<>();

        // 分配三餐热量
        BigDecimal breakfastCalories = targetCalories.multiply(new BigDecimal("0.25"));
        BigDecimal lunchCalories = targetCalories.multiply(new BigDecimal("0.35"));
        BigDecimal dinnerCalories = targetCalories.multiply(new BigDecimal("0.25"));
        BigDecimal snackCalories = targetCalories.multiply(new BigDecimal("0.15"));

        // 根据需求生成不同餐次
        if (!requirements.contains("不想吃主食")) {
            plans.addAll(generateMealWithRequirements(userId, date, "breakfast", breakfastCalories, requirements));
        }

        plans.add(generateSnack(userId, date, "morning_snack", snackCalories.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP)));

        if (!requirements.contains("不想吃主食")) {
            plans.addAll(generateMealWithRequirements(userId, date, "lunch", lunchCalories, requirements));
        }

        plans.add(generateSnack(userId, date, "afternoon_snack", snackCalories.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP)));

        if (!requirements.contains("不想吃主食")) {
            plans.addAll(generateMealWithRequirements(userId, date, "dinner", dinnerCalories, requirements));
        }

        plans.add(generateSnack(userId, date, "evening_snack", snackCalories.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP)));

        // 保存到数据库
        for (DailyPlan plan : plans) {
            dailyPlanMapper.insert(plan);
        }

        return plans;
    }

    private List<DailyPlan> generateMealWithRequirements(Long userId, Date date, String mealType, BigDecimal targetCalories, String requirements) {
        List<DailyPlan> plans = new ArrayList<>();
        List<Food> foods = foodMapper.findAll();

        // 根据需求过滤食材
        if (requirements.contains("不想吃肉类")) {
            foods = foods.stream().filter(f -> !"肉类".equals(f.getCategory())).collect(Collectors.toList());
        }
        if (requirements.contains("对海鲜过敏")) {
            foods = foods.stream().filter(f -> !f.getName().contains("鱼") && !f.getName().contains("虾") && !f.getName().contains("蟹")).collect(Collectors.toList());
        }

        // 创建一个默认用户用于选择食物
        User defaultUser = new User();
        defaultUser.setHealthGoal("维持健康");
        
        // 根据用户信息和健康目标智能选择食物
        List<Food> selectedFoods = aiSelectFoodsByUser(defaultUser, foods, mealType, targetCalories);

        BigDecimal remainingCalories = targetCalories;

        for (Food food : selectedFoods) {
            if (remainingCalories.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal amount = remainingCalories.multiply(new BigDecimal("100"))
                    .divide(food.getCalories(), 2, RoundingMode.HALF_UP);

            if (amount.compareTo(new BigDecimal("200")) > 0) {
                amount = new BigDecimal("200");
            }

            DailyPlan plan = new DailyPlan();
            plan.setUserId(userId);
            plan.setDate(date);
            plan.setMealType(mealType);
            plan.setFoodId(food.getId());
            plan.setFoodName(food.getName());
            plan.setAmount(amount);
            plan.setCalories(food.getCalories().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            plan.setProtein(food.getProtein().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            plan.setCarbs(food.getCarbs().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            plan.setFat(food.getFat().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

            plans.add(plan);
            remainingCalories = remainingCalories.subtract(plan.getCalories());
        }

        return plans;
    }

    private DailyPlan generateSnack(Long userId, Date date, String mealType, BigDecimal targetCalories) {
        // 创建一个默认用户用于生成加餐
        User defaultUser = new User();
        defaultUser.setHealthGoal("维持健康");
        defaultUser.setAge(30);
        defaultUser.setGender(0);
        
        List<Food> snacks = new ArrayList<>();
        
        // 选择适合的加餐
        snacks.addAll(foodMapper.findByCategory("水果"));
        snacks.addAll(foodMapper.findByCategory("奶制品"));
        snacks.addAll(foodMapper.findByCategory("坚果"));

        if (snacks.isEmpty()) {
            snacks = foodMapper.findAll();
        }

        Collections.shuffle(snacks);
        Food food = snacks.get(0);

        BigDecimal amount = targetCalories.multiply(new BigDecimal("100"))
                .divide(food.getCalories(), 2, RoundingMode.HALF_UP);

        DailyPlan plan = new DailyPlan();
        plan.setUserId(userId);
        plan.setDate(date);
        plan.setMealType(mealType);
        plan.setFoodId(food.getId());
        plan.setFoodName(food.getName());
        plan.setAmount(amount);
        plan.setCalories(food.getCalories().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setProtein(food.getProtein().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setCarbs(food.getCarbs().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setFat(food.getFat().multiply(amount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

        return plan;
    }

    // 获取用户的模板列表
    public List<WeeklyPlan> getUserTemplates(Long userId) {
        return weeklyPlanMapper.findTemplatesByUser(userId);
    }

    // 应用周方案到每日
    public Map<String, Object> applyWeeklyPlan(Long userId, Date weekStart) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取周方案
            WeeklyPlan weeklyPlan = weeklyPlanMapper.findByUserAndWeek(userId, weekStart);
            if (weeklyPlan == null) {
                result.put("success", false);
                result.put("message", "未找到周方案");
                return result;
            }

            // 获取周方案的所有日计划
            List<DailyPlan> weeklyPlans = dailyPlanMapper.findByUserAndWeekStart(userId, weekStart);

            // 按日期分组
            Map<Date, List<DailyPlan>> plansByDate = weeklyPlans.stream()
                    .collect(Collectors.groupingBy(DailyPlan::getDate));

            // 应用每一天的方案
            int appliedDays = 0;
            for (Map.Entry<Date, List<DailyPlan>> entry : plansByDate.entrySet()) {
                Date date = entry.getKey();
                List<DailyPlan> dayPlans = entry.getValue();

                // 删除该日期的现有方案
                dailyPlanMapper.deleteByUserAndDate(userId, date);

                // 插入周方案中的日计划
                for (DailyPlan plan : dayPlans) {
                    plan.setId(null); // 重置ID，让数据库自动生成
                    dailyPlanMapper.insert(plan);
                }

                appliedDays++;
            }

            result.put("success", true);
            result.put("message", "周方案应用成功，共应用了" + appliedDays + "天的方案");
            result.put("appliedDays", appliedDays);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "应用周方案失败: " + e.getMessage());
        }

        return result;
    }

    // 获取模板详情
    public Map<String, Object> getTemplateDetail(Long userId, Long templateId) {
        WeeklyPlan template = weeklyPlanMapper.findById(templateId);
        if (template == null || !template.getUserId().equals(userId) || !template.getIsTemplate()) {
            throw new RuntimeException("模板不存在或无权限访问");
        }

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> weeklyPlans = new ArrayList<>();

        // 获取模板中的7天方案
        for (int i = 0; i < 7; i++) {
            LocalDate templateDate = template.getWeekStart().toLocalDate().plusDays(i);
            Date date = Date.valueOf(templateDate);

            List<DailyPlan> dailyPlans = dailyPlanMapper.findByUserAndDate(userId, date);

            Map<String, Object> dayPlan = new HashMap<>();
            dayPlan.put("date", templateDate.toString());
            dayPlan.put("plans", dailyPlans);

            weeklyPlans.add(dayPlan);
        }

        result.put("template", template);
        result.put("dailyPlans", weeklyPlans);

        return result;
    }

    // 更新模板食材
    public Map<String, Object> updateTemplateFood(Long userId, Long planId, Long newFoodId, BigDecimal newAmount, Long templateId, String date) {
        // 验证模板权限
        WeeklyPlan template = weeklyPlanMapper.findById(templateId);
        if (template == null || !template.getUserId().equals(userId) || !template.getIsTemplate()) {
            throw new RuntimeException("模板不存在或无权限访问");
        }

        DailyPlan plan = dailyPlanMapper.findById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new RuntimeException("方案不存在");
        }

        Food newFood = foodMapper.findById(newFoodId);
        if (newFood == null) {
            throw new RuntimeException("食材不存在");
        }

        // 计算营养成分变化
        Map<String, Object> nutritionChange = calculateNutritionChange(plan, newFood, newAmount);

        // DRI符合性检查
        Map<String, Object> driCheck = checkDRICompliance(userId, nutritionChange);

        // 更新方案
        plan.setFoodId(newFoodId);
        plan.setFoodName(newFood.getName());
        plan.setAmount(newAmount);
        plan.setCalories(newFood.getCalories().multiply(newAmount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setProtein(newFood.getProtein().multiply(newAmount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setCarbs(newFood.getCarbs().multiply(newAmount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        plan.setFat(newFood.getFat().multiply(newAmount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));

        dailyPlanMapper.update(plan);

        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);
        result.put("nutritionChange", nutritionChange);
        result.put("driCheck", driCheck);

        return result;
    }

    // 删除模板
    public void deleteTemplate(Long userId, Long templateId) {
        WeeklyPlan template = weeklyPlanMapper.findById(templateId);
        if (template == null || !template.getUserId().equals(userId) || !template.getIsTemplate()) {
            throw new RuntimeException("模板不存在或无权限访问");
        }

        // 删除模板记录
        weeklyPlanMapper.deleteById(templateId);

        // 删除模板关联的日计划（可选，根据业务需求决定）
        // 这里我们保留日计划，只删除模板记录
    }
}


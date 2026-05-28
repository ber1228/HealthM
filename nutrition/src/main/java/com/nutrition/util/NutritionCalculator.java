package com.nutrition.util;

import com.nutrition.entity.Food;
import com.nutrition.entity.MealRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NutritionCalculator {

    private static final BigDecimal DIVISOR = BigDecimal.valueOf(100);

    /**
     * Calculate all 10 nutrients for a given food and amount (in grams).
     * Populates a MealRecord with the calculated values.
     * Reuses the same logic as RecordController.addRecord().
     */
    public static MealRecord calculate(Food food, BigDecimal amount, Long userId,
                                       java.sql.Date date, String mealType) {
        MealRecord record = new MealRecord();
        record.setUserId(userId);
        record.setDate(date);
        record.setMealType(mealType);
        record.setFoodId(food.getId());
        record.setFoodName(food.getName());
        record.setAmount(amount);

        if (food.getCalories() != null) {
            record.setCalories(food.getCalories().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getProtein() != null) {
            record.setProtein(food.getProtein().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getCarbs() != null) {
            record.setCarbs(food.getCarbs().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getFat() != null) {
            record.setFat(food.getFat().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getFiber() != null) {
            record.setFiber(food.getFiber().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getCalcium() != null) {
            record.setCalcium(food.getCalcium().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getIron() != null) {
            record.setIron(food.getIron().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getVitc() != null) {
            record.setVitc(food.getVitc().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getPotassium() != null) {
            record.setPotassium(food.getPotassium().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }
        if (food.getSodium() != null) {
            record.setSodium(food.getSodium().multiply(amount).divide(DIVISOR, 2, RoundingMode.HALF_UP));
        }

        return record;
    }
}

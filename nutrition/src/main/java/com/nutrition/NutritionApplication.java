package com.nutrition;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.nutrition.mapper")
@EnableScheduling
public class NutritionApplication {
    public static void main(String[] args) {
        SpringApplication.run(NutritionApplication.class, args);
    }
}


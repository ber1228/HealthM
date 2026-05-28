package com.nutrition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class NutritionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NutritionBackendApplication.class, args);
    }
}
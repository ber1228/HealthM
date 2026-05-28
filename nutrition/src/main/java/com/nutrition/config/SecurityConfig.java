package com.nutrition.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭默认的CSRF防护（前后端分离项目通常需要关闭）
                .csrf(csrf -> csrf.disable())
                // 允许所有请求无需认证即可访问
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 关闭默认的HTTP Basic认证
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
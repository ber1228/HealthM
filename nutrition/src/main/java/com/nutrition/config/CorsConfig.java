package com.nutrition.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端源（* 表示允许所有源，生产环境建议指定具体域名如http://localhost:3000）
        config.addAllowedOrigin("http://localhost:3000");
        // 允许携带Cookie（登录接口通常需要Cookie/Token，必须开启）
        config.setAllowCredentials(true);
        // 允许的请求方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod("*");
        // 允许的请求头
        config.addAllowedHeader("*");
        // 预检请求的缓存时间（秒），减少OPTIONS请求次数
        config.setMaxAge(3600L);

        // 2. 配置拦截路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有接口生效
        source.registerCorsConfiguration("/**", config);

        // 3. 返回CORS过滤器
        return new CorsFilter(source);
    }
}
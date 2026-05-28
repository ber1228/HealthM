package com.nutrition.config;

import com.nutrition.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtFilter implements Filter {
    
    private final JwtUtil jwtUtil;
    
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // 公共公告路径：仅放行 GET /announcements 与 GET /announcements/{id}
        boolean isPublicAnnouncement = "GET".equalsIgnoreCase(method) && (
                path.matches(".*/announcements$") || path.matches(".*/announcements/\\d+$")
        );
        
        // 公开的AI测试路径 (更宽松的匹配)
        boolean isPublicAiTest = path.contains("/ai/health") || path.contains("/ai/test-public");
        
        // 跳过认证的路径
        if (path.contains("/auth/login") || path.contains("/auth/register") || 
            path.contains("/uploads/") || isPublicAnnouncement || isPublicAiTest) {
            chain.doFilter(request, response);
            return;
        }
        
        // 允许跨域
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                boolean expired = jwtUtil.isTokenExpired(token);
                if (!expired) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    httpRequest.setAttribute("userId", userId);
                    chain.doFilter(request, response);
                    return;
                } else {
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"error\":\"Token Expired\",\"message\":\"令牌已过期，请重新登录\"}");
                    return;
                }
            } catch (ExpiredJwtException e) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"error\":\"Token Expired\",\"message\":\"令牌已过期，请重新登录\"}");
                return;
            } catch (Exception ignored) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"error\":\"Invalid Token\",\"message\":\"无效的令牌，请重新登录\"}");
                return;
            }
        }
        
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"请先登录\"}");
    }
}
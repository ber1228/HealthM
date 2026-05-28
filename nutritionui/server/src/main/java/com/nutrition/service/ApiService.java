package com.nutrition.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class ApiService {
    @Value("${ai-key}")
    private String apiKey;

    public String query(String queryMessage) {
        System.out.println("AI Service query called with message: " + queryMessage);
        
        // 更完善的API密钥检查
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("API密钥未配置");
            return "API密钥未配置，请联系管理员配置AI服务密钥";
        }

        // 设置API密钥
        Constants.apiKey = apiKey;
        System.out.println("Using API Key: " + (apiKey != null ? "已配置" : "未配置"));
        
        try {
            Generation gen = new Generation();
            MessageManager msgManager = new MessageManager(10);
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("你是小陈同学开发的智能食谱营养师，你只回答与食谱相关的问题，不要回答其他问题！")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(queryMessage)
                    .build();
            msgManager.add(systemMsg);
            msgManager.add(userMsg);

            QwenParam param = QwenParam.builder()
                    .model(Generation.Models.QWEN_TURBO)
                    .messages(msgManager.get())
                    .resultFormat(QwenParam.ResultFormat.MESSAGE)
                    .build();

            System.out.println("Calling AI service...");
            GenerationResult result = gen.call(param);
            System.out.println("AI service call completed");
            
            GenerationOutput output = result.getOutput();
            System.out.println("AI Output received: " + (output != null ? "Yes" : "No"));

            if (output != null && output.getChoices() != null && !output.getChoices().isEmpty()) {
                Message message = output.getChoices().get(0).getMessage();
                System.out.println("AI Message received: " + (message != null ? "Yes" : "No"));
                
                if (message != null && message.getContent() != null) {
                    String content = message.getContent().trim();
                    System.out.println("AI Content length: " + content.length());
                    System.out.println("AI Content: " + content);
                    return content.isEmpty() ? "AI生成的内容为空" : content;
                }
            }
            
            System.out.println("AI service returned no valid content");
            return "未能获取到AI响应，请稍后重试";
        } catch (Exception e) {
            System.err.println("AI service exception: " + e.getMessage());
            e.printStackTrace();
            return "智能营养师现在不在线，请稍后再试～ 错误详情：" + e.getMessage();
        }
    }
}
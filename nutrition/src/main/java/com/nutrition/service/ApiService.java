package com.nutrition.service;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiService {
    @Value("${ai-key}")
    private String apiKey;

    // 为每个用户维护对话历史（简单实现，生产环境建议使用Redis等持久化存储）
    private Map<Long, List<Message>> userConversations = new ConcurrentHashMap<>();

    // 单轮对话：不维护对话历史
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
            List<Message> messages = new ArrayList<>();
            
            // 设置系统提示词
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("你是一位专业的营养师，擅长根据用户的身体信息和健康目标提供个性化的饮食建议。")
                    .build();
            messages.add(systemMsg);

            // 添加用户消息
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(queryMessage)
                    .build();
            messages.add(userMsg);

            QwenParam param = QwenParam.builder()
                    .model("qwen-max")
                    .messages(messages)
                    .resultFormat(QwenParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .enableSearch(true)
                    .build();

            GenerationResult result = gen.call(param);
            GenerationOutput output = result.getOutput();

            if (output != null && output.getChoices() != null && !output.getChoices().isEmpty()) {
                String responseText = output.getChoices().get(0).getMessage().getContent();
                System.out.println("AI Response: " + responseText);
                return responseText;
            } else {
                System.err.println("AI返回空结果");
                return "抱歉，暂时无法生成推荐内容，请稍后再试。";
            }
        } catch (Exception e) {
            System.err.println("AI服务调用异常: " + e.getMessage());
            e.printStackTrace();
            return "AI服务调用失败: " + e.getMessage();
        }
    }

    // 多轮对话：维护对话历史
    public String chat(Long userId, String userMessage, String systemPrompt) {
        System.out.println("AI Service chat called with userId: " + userId + ", message: " + userMessage);

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
            
            // 获取或创建用户的对话历史
            List<Message> messages = userConversations.computeIfAbsent(userId, k -> {
                List<Message> msgList = new ArrayList<>();
                // 设置系统提示词
                Message sysMsg = Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content(systemPrompt != null ? systemPrompt : "你是一位专业的营养师，擅长解答用户的饮食和健康问题。")
                        .build();
                msgList.add(sysMsg);
                return msgList;
            });

            // 添加用户消息
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(userMessage)
                    .build();
            messages.add(userMsg);
            
            // 限制历史消息数量
            if (messages.size() > 20) {
                messages.remove(1); // 保留第一条（系统消息），移除后面最早的一条
            }

            QwenParam param = QwenParam.builder()
                    .model("qwen-max")
                    .messages(messages)
                    .resultFormat(QwenParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .enableSearch(true)
                    .build();

            GenerationResult result = gen.call(param);
            GenerationOutput output = result.getOutput();

            if (output != null && output.getChoices() != null && !output.getChoices().isEmpty()) {
                String responseText = output.getChoices().get(0).getMessage().getContent();
                
                // 将AI回复添加到对话历史
                Message assistantMsg = Message.builder()
                        .role(Role.ASSISTANT.getValue())
                        .content(responseText)
                        .build();
                messages.add(assistantMsg);
                
                System.out.println("AI Response: " + responseText);
                return responseText;
            } else {
                System.err.println("AI返回空结果");
                return "抱歉，暂时无法生成回复，请稍后再试。";
            }
        } catch (Exception e) {
            System.err.println("AI服务调用异常: " + e.getMessage());
            e.printStackTrace();
            return "AI服务调用失败: " + e.getMessage();
        }
    }

    // 清除用户对话历史
    public void clearConversation(Long userId) {
        userConversations.remove(userId);
    }

    // 获取当前用户对话历史
    public List<Message> getConversationHistory(Long userId) {
        List<Message> messages = userConversations.get(userId);
        return messages != null ? messages : Collections.emptyList();
    }
}
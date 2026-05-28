package com.nutrition.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationMessage;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalMessageItemImage;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalMessageItemText;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalMessageItemBase;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MultimodalAIService {

    @Value("${ai-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyze a food photo using qwen-vl-max and return identified food items.
     */
    public List<Map<String, Object>> analyzeFoodPhoto(String imageBase64) {
        System.out.println("MultimodalAIService - analyzeFoodPhoto called");

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API密钥未配置");
        }

        try {
            // SDK requires protocol ("http") as first arg; set API key globally
            Constants.apiKey = apiKey;
            MultiModalConversation conv = new MultiModalConversation("http");

            // Build image + text content
            List<MultiModalMessageItemBase> content = new ArrayList<>();
            content.add(new MultiModalMessageItemImage("data:image/jpeg;base64," + imageBase64));
            content.add(new MultiModalMessageItemText(buildPhotoPrompt()));

            MultiModalConversationMessage userMessage = MultiModalConversationMessage.builder()
                    .role(Role.USER.getValue())
                    .content(content)
                    .build();

            List<Object> messages = new ArrayList<>();
            messages.add(userMessage);

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model("qwen-vl-max")
                    .messages(messages)
                    .apiKey(apiKey)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            String responseText = extractResultText(result);

            System.out.println("AI Photo Response: " + responseText);
            return parseJsonArray(responseText);

        } catch (Exception e) {
            System.err.println("图片分析异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("图片分析失败: " + e.getMessage());
        }
    }

    /**
     * Parse natural language food description into structured food items using qwen-max.
     */
    public List<Map<String, Object>> parseFoodFromText(String text) {
        System.out.println("MultimodalAIService - parseFoodFromText called with: " + text);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API密钥未配置");
        }

        Constants.apiKey = apiKey;

        try {
            Generation gen = new Generation();
            List<Message> messages = new ArrayList<>();

            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(buildTextSystemPrompt())
                    .build();
            messages.add(systemMsg);

            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content("请解析以下饮食描述：\n" + text)
                    .build();
            messages.add(userMsg);

            QwenParam param = QwenParam.builder()
                    .model("qwen-max")
                    .messages(messages)
                    .resultFormat(QwenParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .build();

            GenerationResult result = gen.call(param);
            GenerationOutput output = result.getOutput();

            if (output != null && output.getChoices() != null && !output.getChoices().isEmpty()) {
                String responseText = output.getChoices().get(0).getMessage().getContent();
                System.out.println("AI Text Response: " + responseText);
                return parseJsonArray(responseText);
            } else {
                throw new RuntimeException("AI返回空结果");
            }
        } catch (Exception e) {
            System.err.println("文本解析异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文本解析失败: " + e.getMessage());
        }
    }

    /**
     * Estimate nutrition for a food item not in the database, using AI.
     */
    public Map<String, Object> estimateNutrition(String foodName, int weightGrams) {
        System.out.println("MultimodalAIService - estimateNutrition called for: " + foodName + " " + weightGrams + "g");

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API密钥未配置");
        }

        Constants.apiKey = apiKey;

        try {
            Generation gen = new Generation();
            List<Message> messages = new ArrayList<>();

            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(buildNutritionEstimatePrompt(foodName, weightGrams))
                    .build();
            messages.add(systemMsg);

            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content("请估算营养成分")
                    .build();
            messages.add(userMsg);

            QwenParam param = QwenParam.builder()
                    .model("qwen-max")
                    .messages(messages)
                    .resultFormat(QwenParam.ResultFormat.MESSAGE)
                    .topP(0.8)
                    .build();

            GenerationResult result = gen.call(param);
            GenerationOutput output = result.getOutput();

            if (output != null && output.getChoices() != null && !output.getChoices().isEmpty()) {
                String responseText = output.getChoices().get(0).getMessage().getContent();
                System.out.println("AI Nutrition Estimate Response: " + responseText);
                List<Map<String, Object>> list = parseJsonArray(responseText);
                return list.isEmpty() ? null : list.get(0);
            }
            return null;
        } catch (Exception e) {
            System.err.println("营养估算异常: " + e.getMessage());
            return null;
        }
    }

    private String extractResultText(MultiModalConversationResult result) {
        if (result == null || result.getOutput() == null
                || result.getOutput().getChoices() == null
                || result.getOutput().getChoices().isEmpty()) {
            throw new RuntimeException("AI返回空结果");
        }

        // getMessage() returns MultiModalMessage, getContent() returns List<Map<String, Object>>
        Object content = result.getOutput().getChoices().get(0).getMessage().getContent();

        if (content instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> contentList = (List<Map<String, Object>>) content;
            if (!contentList.isEmpty() && contentList.get(0).containsKey("text")) {
                return contentList.get(0).get("text").toString();
            }
        } else if (content instanceof String) {
            return (String) content;
        }

        return content.toString();
    }

    private String buildPhotoPrompt() {
        return "你是一个专业的营养师。请仔细分析这张食物图片，识别出所有食物并估算每种食物的重量（克）。\n\n" +
                "请严格以JSON数组格式回复，不要包含任何其他文字说明：\n" +
                "[{\"name\":\"食物名称\",\"weight\":估算克数,\"category\":\"分类\"}]\n\n" +
                "分类只能是以下之一：谷物、蛋白质、蔬菜、水果、奶制品、豆制品、坚果、其他\n" +
                "请根据常见的餐具和份量来估算重量。如果图片中有碗，请根据碗的大小估算食物重量。";
    }

    private String buildTextSystemPrompt() {
        return "你是一个专业的营养师。请解析用户给出的自然语言饮食描述，提取出所有食物、估算重量（克）和餐次。\n\n" +
                "请严格以JSON数组格式回复，不要包含任何其他文字说明：\n" +
                "[{\"name\":\"食物名称\",\"weight\":估算克数,\"mealType\":\"餐次\"}]\n\n" +
                "餐次只能是以下之一：breakfast(早餐)、lunch(午餐)、dinner(晚餐)、morning_snack(上午加餐)、afternoon_snack(下午加餐)、evening_snack(晚上加餐)\n" +
                "如果用户描述中没有明确提到餐次，请根据描述推断最可能的餐次（如\"中午\"对应lunch）。\n" +
                "常见食物重量参考：一碗米饭约200g，一碗面条约300g，一个鸡蛋约60g，一杯豆浆约250ml，一个馒头约100g，一盘炒菜约200g。";
    }

    private String buildNutritionEstimatePrompt(String foodName, int weightGrams) {
        return "你是一个专业的营养师。请估算以下食物的营养成分（按" + weightGrams + "克计算）：\n\n" +
                "食物名称：" + foodName + "\n重量：" + weightGrams + "克\n\n" +
                "请严格以JSON数组格式回复（只有一个元素），不要包含任何其他文字说明：\n" +
                "[{\"calories\":千卡,\"protein\":蛋白质克数,\"carbs\":碳水克数,\"fat\":脂肪克数,\"fiber\":纤维克数," +
                "\"calcium\":钙毫克,\"iron\":铁毫克,\"vitc\":维C毫克,\"potassium\":钾毫克,\"sodium\":钠毫克}]\n\n" +
                "请基于中国食物成分表的数据进行合理估算。";
    }

    private List<Map<String, Object>> parseJsonArray(String responseText) {
        try {
            // Strip markdown code fences if present
            String cleaned = responseText.replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            // Extract JSON array from response
            int start = cleaned.indexOf('[');
            int end = cleaned.lastIndexOf(']') + 1;
            if (start >= 0 && end > start) {
                cleaned = cleaned.substring(start, end);
            }

            return objectMapper.readValue(cleaned, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            System.err.println("JSON解析失败: " + e.getMessage());
            System.err.println("原始文本: " + responseText);
            return new ArrayList<>();
        }
    }
}

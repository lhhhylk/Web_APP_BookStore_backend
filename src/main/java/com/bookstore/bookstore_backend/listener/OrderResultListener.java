package com.bookstore.bookstore_backend.listener;

import com.bookstore.bookstore_backend.websocket.OrderWebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;  // 新增导入
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class OrderResultListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderResultListener.class);

    @Autowired
    private OrderWebSocketServer webSocketServer;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "order_topic_result", groupId = "order-result-group", containerFactory = "stringKafkaListenerContainerFactory")
    public void handleOrderResult(String result) {
        logger.info("收到订单处理结果: {}", result);
        String[] parts = result.split(",");
        if (parts.length < 3) {
            logger.error("无效的结果消息格式: {}", result);
            return;
        }
        String userIdStr = parts[0].trim();
        String orderId = parts[1].trim();
        String status = parts[2].trim();

        // 用 Map + ObjectMapper 构建 JSON，避免转义问题，并添加 type
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("type", "order_update");
        messageMap.put("orderId", orderId);
        messageMap.put("userId", userIdStr);
        messageMap.put("status", status.toLowerCase());  // 规范化为小写
        messageMap.put("message", status.equals("SUCCESS") ? "订单处理完成，感谢购买！" : "订单处理失败，请重试。");
        messageMap.put("timestamp", Instant.now().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT));

        try {
            String jsonMessage = objectMapper.writeValueAsString(messageMap);
            webSocketServer.sendMessageToUser(userIdStr, jsonMessage);
        } catch (Exception e) {
            logger.error("JSON 构建失败: {}", e.getMessage());
        }
    }
}
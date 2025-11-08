package com.bookstore.bookstore_backend.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/transfer/{userId}")
@Component
public class OrderWebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(OrderWebSocketServer.class);
    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        if (SESSIONS.get(userId) != null) {
            logger.warn("用户 {} 已在线，忽略新连接", userId);
            return;
        }
        SESSIONS.put(userId.trim(), session);
        logger.info("用户 {} WebSocket 连接开启: {}", userId, session.getId());
    }

    // 修改：添加 Session 参数，从 PathParameters 获取 userId（更可靠）
    @OnClose
    public void onClose(Session session) {
        String userId = session.getPathParameters().getOrDefault("userId", "unknown").trim();
        SESSIONS.remove(userId);
        logger.info("用户 {} WebSocket 连接关闭", userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("WebSocket 错误: {}", error.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("收到客户端消息: {}", message);
    }

    // 修改：userId to String
    public static void sendMessageToUser(String userId, String jsonMessage) {  // userId 已为 String
        Session session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(jsonMessage);
                logger.info("发送给用户 {}: {}", userId, jsonMessage);
            } catch (IOException e) {
                logger.error("发送 WebSocket 消息失败: {}", e.getMessage());
                SESSIONS.remove(userId);
            }
        } else {
            logger.warn("用户 {} 不在线或 Session 已关闭", userId);
        }
    }
}
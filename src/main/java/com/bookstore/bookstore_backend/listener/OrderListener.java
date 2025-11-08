package com.bookstore.bookstore_backend.listener;

import com.bookstore.bookstore_backend.model.order.Order;
import com.bookstore.bookstore_backend.model.order.OrderMessage;
import com.bookstore.bookstore_backend.services.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderListener.class);

    @Autowired
    private IOrderService orderService;

    @Autowired
    private KafkaTemplate<String, String> stringKafkaTemplate;

    // 指定 containerFactory，并处理 Long to String
    @KafkaListener(topics = "order_topic", groupId = "order-group", containerFactory = "orderMessageKafkaListenerContainerFactory")
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderMessage(OrderMessage orderMessage) {
        try {
            logger.info("收到下单消息: {}, 开始处理订单...", orderMessage);
            Order order = orderService.createOrder(orderMessage.getUserId(), orderMessage.getOrderDTO());
            // 修改：userId to String
            String result = String.valueOf(orderMessage.getUserId()) + "," + order.getId() + ",SUCCESS";
            stringKafkaTemplate.send("order_topic_result", result);
            logger.info("订单处理成功，结果消息已发送: {}", result);
        } catch (Exception e) {
            logger.error("订单处理失败: {}", e.getMessage());
            // 修改：userId to String
            String failResult = String.valueOf(orderMessage.getUserId()) + ",N/A,FAILED";
            stringKafkaTemplate.send("order_topic_result", failResult);
            throw e;
        }
    }
}
package com.bookstore.bookstore_backend.controller;

import com.bookstore.bookstore_backend.model.ResponseMessage;
import com.bookstore.bookstore_backend.model.User.User;
import com.bookstore.bookstore_backend.model.order.*;
import com.bookstore.bookstore_backend.repository.UserRepository;
import com.bookstore.bookstore_backend.services.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, OrderMessage> kafkaTemplate;

    @GetMapping("/get")
    public ResponseMessage<List<OrderResponseDTO>> getUserOrders() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return ResponseMessage.success(orderService.getUserOrders(user.getId()));
    }

    @PostMapping("/add")
    public ResponseMessage<String> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

            System.out.println("Received order request from user: " + user.getId() + " with items: " + orderDTO.getItems().size());
            OrderMessage orderMessage = new OrderMessage(user.getId(), orderDTO);
            kafkaTemplate.send("order_topic", orderMessage);

            System.out.println("Order message sent to Kafka: " + orderMessage);
            return ResponseMessage.success("订单已提交，正在异步处理");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseMessage<List<Order>> searchUserOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return ResponseMessage.success(orderService.searchUserOrders(user.getId(), keyword, startTime, endTime));
    }

    @GetMapping("/admin/search")
    public ResponseMessage<List<Order>> searchAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseMessage.success(orderService.searchAllOrders(keyword, startTime, endTime));
    }

    @GetMapping("/statistics")
    public ResponseMessage<OrderStatisticsDTO> getUserOrderStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        OrderStatisticsDTO stats = orderService.getUserOrderStatistics(user.getId(), startTime, endTime);
        return ResponseMessage.success(stats);
    }
}
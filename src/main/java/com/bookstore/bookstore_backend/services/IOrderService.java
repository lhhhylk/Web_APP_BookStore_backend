package com.bookstore.bookstore_backend.services;

import com.bookstore.bookstore_backend.model.order.Order;
import com.bookstore.bookstore_backend.model.order.OrderDTO;
import com.bookstore.bookstore_backend.model.order.OrderResponseDTO;
import com.bookstore.bookstore_backend.model.order.OrderStatisticsDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {
    List<OrderResponseDTO> getUserOrders(Long userId);
    Order createOrder(Long userId, OrderDTO orderDTO);
    List<Order> searchUserOrders(Long userId, String keyword, LocalDateTime startTime, LocalDateTime endTime);
    List<Order> searchAllOrders(String keyword, LocalDateTime startTime, LocalDateTime endTime);
    OrderStatisticsDTO getUserOrderStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime);
}
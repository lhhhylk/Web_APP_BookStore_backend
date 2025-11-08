package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
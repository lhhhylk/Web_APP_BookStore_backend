package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.order.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"orderItems", "orderItems.book"})
    List<Order> findByUserId(Long userId);

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.user " +
            "WHERE o.user.id = :userId " +
            "AND (:startTime IS NULL OR o.createdAt >= :startTime) " +
            "AND (:endTime IS NULL OR o.createdAt <= :endTime) " +
            "AND EXISTS (SELECT oi FROM o.orderItems oi WHERE :keyword IS NULL OR LOWER(oi.book.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Order> searchOrdersByBookTitleAndTime(@Param("userId") Long userId,
                                               @Param("keyword") String keyword,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.user " +
            "JOIN FETCH o.orderItems oi " +
            "JOIN FETCH oi.book " +
            "WHERE (:startTime IS NULL OR o.createdAt >= :startTime) " +
            "AND (:endTime IS NULL OR o.createdAt <= :endTime) " +
            "AND EXISTS (SELECT oi2 FROM o.orderItems oi2 WHERE :keyword IS NULL OR LOWER(oi2.book.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Order> searchAllOrders(@Param("keyword") String keyword,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);
}
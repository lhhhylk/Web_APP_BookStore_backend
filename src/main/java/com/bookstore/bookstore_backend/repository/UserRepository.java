package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.User.User;
import com.bookstore.bookstore_backend.model.User.UserConsumptionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAll();
    @Query("SELECT " +
            "u.id AS userId, " +
            "u.username AS username, " +
            "u.email AS email, " +
            "COALESCE(SUM(CAST(oi.book.price AS double) * oi.number), 0.0) AS totalConsumption " +
            "FROM User u " +
            "LEFT JOIN u.orders o " +
            "LEFT JOIN o.orderItems oi " +
            "GROUP BY u.id, u.username, u.email " +
            "ORDER BY COALESCE(SUM(CAST(oi.book.price AS double) * oi.number), 0.0) DESC")
    List<UserConsumptionProjection> findUserConsumptionRanking();
}
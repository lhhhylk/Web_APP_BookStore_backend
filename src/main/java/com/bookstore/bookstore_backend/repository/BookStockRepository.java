package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.book.BookStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookStockRepository extends JpaRepository<BookStock, Long> {
    // **核心查询：按 bookId 找库存（1:1）**
    Optional<BookStock> findByBookId(Long bookId);

}
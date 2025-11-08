package com.bookstore.bookstore_backend.model.book;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "book_stock")
public class BookStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false, unique = true)
    private Long bookId;

    @Column(name = "inventory", nullable = false)
    private Integer inventory = 100;

    @Override
    public String toString() {
        return "BookStock{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", inventory=" + inventory +
                '}';
    }
}
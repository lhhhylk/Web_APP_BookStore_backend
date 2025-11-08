package com.bookstore_author_search.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 书籍实体（简化版，只包含必要的字段）
 */
@Entity
@Table(name = "books")
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private boolean deleted = false;
}

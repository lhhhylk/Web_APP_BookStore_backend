package com.bookstore.bookstore_backend.model.book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作者服务响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {
    private String title;
    private String author;
    private String message;
    private boolean success;
}


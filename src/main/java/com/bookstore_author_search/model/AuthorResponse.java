package com.bookstore_author_search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作者查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {
    private String title;
    private String author;
    private String message;
    private boolean success;

    public static AuthorResponse success(String title, String author) {
        return new AuthorResponse(title, author, "查询成功", true);
    }

    public static AuthorResponse failure(String title, String message) {
        return new AuthorResponse(title, null, message, false);
    }
}

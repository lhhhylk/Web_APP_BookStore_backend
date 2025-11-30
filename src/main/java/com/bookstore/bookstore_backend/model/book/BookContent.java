package com.bookstore.bookstore_backend.model.book;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "book_contents")
public class BookContent {
    @Id
    private Long bookId;

    private String cover;

    private String description;
}


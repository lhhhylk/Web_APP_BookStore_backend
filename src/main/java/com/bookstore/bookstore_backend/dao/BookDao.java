package com.bookstore.bookstore_backend.dao;

import com.bookstore.bookstore_backend.model.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookDao {
    List<List<String>> findAllTags();
    Page<Book> findBooksByKeywordAndTagWithPagination(String keyword, String tag, Pageable pageable);
    List<Book> findBooksByKeywordAndTag(String keyword, String tag);
    long countBooksByKeywordAndTag(String keyword, String tag);
    Book save(Book book);
    void deleteById(Long id);
    Optional<Book> findById(Long id);
}
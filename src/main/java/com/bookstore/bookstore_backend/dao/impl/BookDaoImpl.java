package com.bookstore.bookstore_backend.dao.impl;

import com.bookstore.bookstore_backend.dao.BookDao;
import com.bookstore.bookstore_backend.model.book.Book;
import com.bookstore.bookstore_backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BookDaoImpl implements BookDao {

    @Autowired
    private BookRepository bookRepository;

    @Override
    public List<List<String>> findAllTags() {
        return bookRepository.findAllTagsByDeletedFalse();
    }

    @Override
    public Page<Book> findBooksByKeywordAndTagWithPagination(String keyword, String tag, Pageable pageable) {
        return bookRepository.findBooksByKeywordAndTagWithPaginationAndNotDeleted(keyword, tag, pageable);
    }

    @Override
    public List<Book> findBooksByKeywordAndTag(String keyword, String tag) {
        return bookRepository.findBooksByKeywordAndTagAndNotDeleted(keyword, tag);
    }

    @Override
    public long countBooksByKeywordAndTag(String keyword, String tag) {
        return bookRepository.countBooksByKeywordAndTagAndNotDeleted(keyword, tag);
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }
}
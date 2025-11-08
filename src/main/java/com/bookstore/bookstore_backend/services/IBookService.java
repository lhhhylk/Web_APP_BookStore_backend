package com.bookstore.bookstore_backend.services;

import com.bookstore.bookstore_backend.model.book.Book;
import com.bookstore.bookstore_backend.model.book.BookDTO;
import com.bookstore.bookstore_backend.model.comment.Comment;
import com.bookstore.bookstore_backend.model.comment.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface IBookService {
    List<Book> getAllBooks();
    Page<Book> getBooks(String keyword, String tag, Pageable pageable);
    long countBooks(String keyword, String tag);
    Book getBookById(Long id);
    Book saveBook(Book book);
    void deleteBook(Long id);
    Comment addCommentToBook(Long bookId, CommentDTO comment);
    List<Comment> getCommentsByBookId(Long bookId);
    Set<String> getAllTags();
    Book updateBook(Long id, BookDTO bookDTO);
    List<Book> getBooksOrderBySalesDesc();
}

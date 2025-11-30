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
    
    /**
     * 基于标签图搜索图书
     * 根据用户选中的标签，在Neo4j中查找通过2跳可以关联到的所有标签，
     * 然后在MySQL中搜索所有带有这些标签中任意一个或多个的图书
     * @param keyword 关键词（可为null）
     * @param tag 用户选中的标签
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Book> getBooksByTagGraph(String keyword, String tag, Pageable pageable);
    
    /**
     * 基于标签图搜索图书（多个标签）
     * @param keyword 关键词（可为null）
     * @param tags 用户选中的标签列表
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Book> getBooksByTagGraph(String keyword, List<String> tags, Pageable pageable);
    
    /**
     * 统计基于标签图搜索的图书数量
     */
    long countBooksByTagGraph(String keyword, String tag);
    
    /**
     * 统计基于标签图搜索的图书数量（多个标签）
     */
    long countBooksByTagGraph(String keyword, List<String> tags);
}

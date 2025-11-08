package com.bookstore_author_search.service;

import com.bookstore_author_search.model.AuthorResponse;
import com.bookstore_author_search.model.Book;
import com.bookstore_author_search.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 作者查询服务
 */
@Service
public class AuthorService {

    @Autowired
    private BookRepository bookRepository;

    /**
     * 根据书名查询作者
     * @param title 书名
     * @return 作者信息
     */
    public AuthorResponse getAuthorByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return AuthorResponse.failure("", "书名不能为空");
        }

        // 先尝试精确匹配
        Optional<Book> book = bookRepository.findByTitleIgnoreCaseAndNotDeleted(title.trim());

        // 如果精确匹配失败，尝试模糊匹配
        if (book.isEmpty()) {
            book = bookRepository.findByTitleContainingIgnoreCaseAndNotDeleted(title.trim());
        }

        if (book.isPresent()) {
            return AuthorResponse.success(book.get().getTitle(), book.get().getAuthor());
        } else {
            return AuthorResponse.failure(title, "未找到书名: " + title);
        }
    }
}


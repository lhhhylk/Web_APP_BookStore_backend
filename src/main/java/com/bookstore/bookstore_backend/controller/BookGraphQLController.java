package com.bookstore.bookstore_backend.controller;

import com.bookstore.bookstore_backend.model.book.Book;
import com.bookstore.bookstore_backend.services.IBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * 使用 GraphQL 按书名查询图书的控制器
 * 复用现有的 Service / Repository / DAO / Entity 逻辑
 */
@Controller
public class BookGraphQLController {

    @Autowired
    private IBookService bookService;

    /**
     * GraphQL 查询：根据书名（模糊匹配）搜索图书
     *
     * @param title 书名关键字
     * @return 匹配的图书列表
     */
    @QueryMapping
    public List<Book> booksByTitle(@Argument String title) {
        // 复用现有的按关键字查询逻辑（内部已按 title/author 模糊匹配，并经 DAO/Repository 访问数据库）
        Pageable pageable = PageRequest.of(0, 100); // 简单返回前 100 条结果
        Page<Book> page = bookService.getBooks(title, null, pageable);
        return page.getContent();
    }
}


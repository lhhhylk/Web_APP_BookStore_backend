package com.bookstore_author_search.controller;

import com.bookstore_author_search.model.AuthorResponse;
import com.bookstore_author_search.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author Service控制器
 * 提供根据书名查询作者的REST API
 */
@RestController
@RequestMapping("/api")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    /**
     * 根据书名查询作者
     * GET /api/author?title={书名}
     *
     * @param title 书名
     * @return 作者信息
     */
    @GetMapping("/author")
    public AuthorResponse getAuthorByTitle(@RequestParam String title) {
        return authorService.getAuthorByTitle(title);
    }
}
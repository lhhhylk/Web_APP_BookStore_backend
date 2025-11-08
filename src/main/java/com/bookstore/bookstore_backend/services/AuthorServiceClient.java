package com.bookstore.bookstore_backend.services;

import com.bookstore.bookstore_backend.model.book.AuthorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Author Service的Feign客户端接口
 * 用于调用Author Service微服务
 */
@FeignClient(name = "author-service", path = "/api")
public interface AuthorServiceClient {
    
    /**
     * 根据书名查询作者
     * @param title 书名
     * @return 作者信息
     */
    @GetMapping("/author")
    AuthorResponse getAuthorByTitle(@RequestParam("title") String title);
}


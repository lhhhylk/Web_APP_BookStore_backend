package com.bookstore_author_search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Author Service微服务
 * 提供根据书名查询作者的功能
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务发现客户端（支持Eureka），注册到Eureka Server
public class BookstoreAuthorSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreAuthorSearchApplication.class, args);
    }
}


package com.bookstore_eureka_server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Eureka服务注册中心
 * 提供服务发现和路由功能
 */
@SpringBootApplication
@EnableEurekaServer  // 启用Eureka Server
public class BookstoreEurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookstoreEurekaServerApplication.class, args);
    }
}

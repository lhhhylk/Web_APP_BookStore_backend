package com.bookstore_price_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 价格计算微服务主应用类
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务发现客户端（支持Eureka）
public class BookstorePriceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstorePriceServiceApplication.class, args);
    }

}

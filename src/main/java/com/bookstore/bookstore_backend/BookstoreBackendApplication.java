package com.bookstore.bookstore_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)  // 启用CGLIB代理，支持session scope
@EnableDiscoveryClient  // 启用服务发现客户端（支持Eureka）
@EnableFeignClients  // 启用Feign客户端
public class BookstoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreBackendApplication.class, args);
    }

}

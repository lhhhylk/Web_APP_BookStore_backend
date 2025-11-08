package com.bookstore.bookstore_backend.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 价格计算服务 Feign 客户端接口
 * 用于调用价格计算微服务
 */
@FeignClient(name = "price-calculation-service", path = "/api")
public interface PriceCalculationServiceClient {
    
    /**
     * 计算订单中某种书的总价（POST方式）
     * @param request 价格计算请求，包含单价和数量
     * @return 价格计算响应，包含总价
     */
    @PostMapping("/calculate")
    Map<String, Object> calculatePrice(@RequestBody Map<String, Object> request);

    /**
     * 计算订单中某种书的总价（GET方式）
     * @param unitPrice 单价
     * @param quantity 数量
     * @return 价格计算响应，包含总价
     */
    @GetMapping("/calculate")
    Map<String, Object> calculatePriceGet(
            @RequestParam("unitPrice") String unitPrice,
            @RequestParam("quantity") int quantity);
}


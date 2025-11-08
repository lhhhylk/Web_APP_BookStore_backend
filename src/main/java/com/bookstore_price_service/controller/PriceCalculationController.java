package com.bookstore_price_service.controller;

import com.bookstore_price_service.dto.PriceCalculationRequest;
import com.bookstore_price_service.dto.PriceCalculationResponse;
import com.bookstore_price_service.service.PriceCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 价格计算控制器
 */
@RestController
@RequestMapping("/api")
public class PriceCalculationController {

    @Autowired
    private PriceCalculationService priceCalculationService;

    /**
     * 计算订单中某种书的总价
     * @param request 价格计算请求（包含单价和数量）
     * @return 价格计算响应（包含总价）
     */
    @PostMapping("/calculate")
    public PriceCalculationResponse calculatePrice(@RequestBody PriceCalculationRequest request) {
        return priceCalculationService.calculateLineTotal(request);
    }

    /**
     * GET方式计算订单中某种书的总价（用于测试）
     * @param unitPrice 单价
     * @param quantity 数量
     * @return 价格计算响应（包含总价）
     */
    @GetMapping("/calculate")
    public PriceCalculationResponse calculatePriceGet(
            @RequestParam("unitPrice") String unitPrice,
            @RequestParam("quantity") int quantity) {
        PriceCalculationRequest request = new PriceCalculationRequest();
        request.setUnitPrice(unitPrice);
        request.setQuantity(quantity);
        return priceCalculationService.calculateLineTotal(request);
    }
}


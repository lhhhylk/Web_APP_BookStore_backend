package com.bookstore_price_service.service;

import com.bookstore_price_service.dto.PriceCalculationRequest;
import com.bookstore_price_service.dto.PriceCalculationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 价格计算服务实现
 */
@Service
public class PriceCalculationService {

    /**
     * 计算订单中某种书的总价
     * @param request 价格计算请求
     * @return 价格计算响应
     */
    public PriceCalculationResponse calculateLineTotal(PriceCalculationRequest request) {
        if (request.getUnitPrice() == null || request.getUnitPrice().trim().isEmpty()) {
            throw new IllegalArgumentException("单价不能为空");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }

        try {
            BigDecimal price = new BigDecimal(request.getUnitPrice());
            BigDecimal quantity = BigDecimal.valueOf(request.getQuantity());
            BigDecimal total = price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

            PriceCalculationResponse response = new PriceCalculationResponse();
            response.setUnitPrice(request.getUnitPrice());
            response.setQuantity(request.getQuantity());
            response.setTotalPrice(total.toString());
            response.setSuccess(true);
            response.setMessage("计算成功");

            return response;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("单价格式不正确: " + request.getUnitPrice());
        }
    }
}


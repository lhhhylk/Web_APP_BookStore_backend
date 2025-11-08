package com.bookstore.bookstore_backend.services.Impl;

import com.bookstore.bookstore_backend.services.IPriceCalculationService;
import com.bookstore.bookstore_backend.services.PriceCalculationServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 价格计算服务实现
 * 通过 Feign Client 调用价格计算微服务
 */
@Service
public class PriceCalculationService implements IPriceCalculationService {

    @Autowired(required = false)
    private PriceCalculationServiceClient priceCalculationServiceClient;

    /**
     * 计算订单中某种书的总价
     * 优先使用微服务，如果微服务不可用则使用本地计算
     * 
     * @param unitPrice 单价
     * @param quantity 数量
     * @return 总价
     */
    @Override
    public BigDecimal calculateLineTotal(String unitPrice, int quantity) {
        if (unitPrice == null) {
            throw new IllegalArgumentException("单价不能为空");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }

        // 如果 Feign Client 可用，则调用微服务
        if (priceCalculationServiceClient != null) {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("unitPrice", unitPrice);
                request.put("quantity", quantity);

                Map<String, Object> response = priceCalculationServiceClient.calculatePrice(request);
                
                // 检查响应是否成功
                Boolean success = (Boolean) response.get("success");
                if (Boolean.TRUE.equals(success)) {
                    String totalPrice = (String) response.get("totalPrice");
                    return new BigDecimal(totalPrice);
                } else {
                    String message = (String) response.get("message");
                    throw new IllegalArgumentException("价格计算失败: " + message);
                }
            } catch (Exception e) {
                // 如果微服务调用失败，降级到本地计算
                System.err.println("价格计算微服务调用失败，使用本地计算: " + e.getMessage());
                return calculateLocally(unitPrice, quantity);
            }
        } else {
            // 如果 Feign Client 不可用，使用本地计算
            return calculateLocally(unitPrice, quantity);
        }
    }

    /**
     * 本地计算价格（降级方案）
     * @param unitPrice 单价
     * @param quantity 数量
     * @return 总价
     */
    private BigDecimal calculateLocally(String unitPrice, int quantity) {
        BigDecimal price = new BigDecimal(unitPrice);
        BigDecimal qty = BigDecimal.valueOf(quantity);
        return price.multiply(qty).setScale(2, RoundingMode.HALF_UP);
    }
}



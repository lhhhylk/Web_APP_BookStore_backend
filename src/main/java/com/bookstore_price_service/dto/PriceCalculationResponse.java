package com.bookstore_price_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 价格计算响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationResponse {
    /**
     * 单价
     */
    private String unitPrice;

    /**
     * 数量
     */
    private int quantity;

    /**
     * 总价
     */
    private String totalPrice;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;
}

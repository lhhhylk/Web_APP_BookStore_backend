package com.bookstore_price_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 价格计算请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequest {
    /**
     * 单价（字符串格式，避免精度丢失）
     */
    private String unitPrice;

    /**
     * 数量
     */
    private int quantity;
}



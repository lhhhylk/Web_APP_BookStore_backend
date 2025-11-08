package com.bookstore_price_service.exception;

import com.bookstore_price_service.dto.PriceCalculationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PriceCalculationResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        PriceCalculationResponse response = new PriceCalculationResponse();
        response.setSuccess(false);
        response.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PriceCalculationResponse> handleException(Exception e) {
        PriceCalculationResponse response = new PriceCalculationResponse();
        response.setSuccess(false);
        response.setMessage("服务器内部错误: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}



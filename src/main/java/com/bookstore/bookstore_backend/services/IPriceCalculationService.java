package com.bookstore.bookstore_backend.services;

import java.math.BigDecimal;

public interface IPriceCalculationService {
    BigDecimal calculateLineTotal(String unitPrice, int quantity);
}



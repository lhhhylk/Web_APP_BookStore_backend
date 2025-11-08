package com.bookstore.bookstore_backend.dao;

import com.bookstore.bookstore_backend.model.cart.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartItemDao {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);
    CartItem save(CartItem cartItem);
    void deleteById(Long id);
    Optional<CartItem> findById(Long id);
}
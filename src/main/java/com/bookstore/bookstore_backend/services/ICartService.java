package com.bookstore.bookstore_backend.services;

import com.bookstore.bookstore_backend.model.cart.CartItem;

import java.util.List;

public interface ICartService {
    List<CartItem> getUserCart(Long userId);
    CartItem addBookToCart(Long userId, Long bookId, int quantity);
    void removeCartItem(Long cartItemId);
    CartItem updateCartItemQuantity(Long cartItemId, int quantity);
}
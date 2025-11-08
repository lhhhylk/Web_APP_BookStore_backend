package com.bookstore.bookstore_backend.dao.impl;

import com.bookstore.bookstore_backend.dao.CartItemDao;
import com.bookstore.bookstore_backend.model.cart.CartItem;
import com.bookstore.bookstore_backend.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CartItemDaoImpl implements CartItemDao {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Override
    public List<CartItem> findByUserId(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    public Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId) {
        return cartItemRepository.findByUserIdAndBookId(userId, bookId);
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }

    @Override
    public void deleteById(Long id) {
        cartItemRepository.deleteById(id);
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return cartItemRepository.findById(id);
    }
}
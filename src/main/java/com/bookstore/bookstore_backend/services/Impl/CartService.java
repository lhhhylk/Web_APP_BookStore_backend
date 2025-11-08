package com.bookstore.bookstore_backend.services.Impl;

import com.bookstore.bookstore_backend.model.User.User;
import com.bookstore.bookstore_backend.model.book.Book;
import com.bookstore.bookstore_backend.model.cart.CartItem;
import com.bookstore.bookstore_backend.repository.BookRepository;
import com.bookstore.bookstore_backend.repository.CartItemRepository;
import com.bookstore.bookstore_backend.repository.UserRepository;
import com.bookstore.bookstore_backend.services.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService implements ICartService {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<CartItem> getUserCart(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("无效的用户id，该用户不存在"));
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public CartItem addBookToCart(Long userId, Long bookId, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("书籍不存在"));


        Optional<CartItem> existingCartItem = cartItemRepository.findByUserIdAndBookId(userId, bookId);
        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setNumber(cartItem.getNumber() + quantity);
            return cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setBook(book);
            cartItem.setNumber(quantity);
            return cartItemRepository.save(cartItem);
        }
    }

    @Override
    @Transactional
    public void removeCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public CartItem updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("购物车项不存在"));
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        cartItem.setNumber(quantity);
        return cartItemRepository.save(cartItem);
    }
}
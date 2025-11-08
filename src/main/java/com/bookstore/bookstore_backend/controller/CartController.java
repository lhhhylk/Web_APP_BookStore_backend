package com.bookstore.bookstore_backend.controller;

import com.bookstore.bookstore_backend.model.ResponseMessage;
import com.bookstore.bookstore_backend.model.User.User;
import com.bookstore.bookstore_backend.model.cart.CartItem;
import com.bookstore.bookstore_backend.model.cart.CartItemDTO;
import com.bookstore.bookstore_backend.repository.UserRepository;
import com.bookstore.bookstore_backend.services.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private ICartService cartService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/get")
    public ResponseMessage<List<CartItem>> getUserCart() {
        // 从安全上下文获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // 根据用户名查询用户ID
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return ResponseMessage.success(cartService.getUserCart(user.getId()));
    }

    @PostMapping("/add")
    public ResponseMessage<CartItem> addBookToCart(@RequestBody CartItemDTO cartItemDTO) {
        try {
            // 从安全上下文获取当前用户名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            // 根据用户名查询用户ID
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

            // 覆盖DTO中的用户ID
            cartItemDTO.setUserId(user.getId());

            Long bookId = cartItemDTO.getBookId();
            int quantity = cartItemDTO.getNumber();
            CartItem cartItem = cartService.addBookToCart(user.getId(), bookId, quantity);
            return ResponseMessage.success(cartItem);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/delete/{cartItemId}")
    public ResponseMessage removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseMessage.success(null);
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseMessage<CartItem> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody CartItemDTO cartItemDTO) {
        CartItem cartItem = cartService.updateCartItemQuantity(cartItemId, cartItemDTO.getNumber());
        return ResponseMessage.success(cartItem);
    }
}
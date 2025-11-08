package com.bookstore.bookstore_backend.model.cart;

import com.bookstore.bookstore_backend.model.book.Book;

public class CartItemDTO {
    private Long id;
    private Long bookId;
    private int number;
    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    @Override
    public String toString() {
        return "CartItemDTO{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", number=" + number +
                ", userId=" + userId +
                '}';
    }
}

package com.bookstore.bookstore_backend.model.cart;

import com.bookstore.bookstore_backend.model.User.User;
import com.bookstore.bookstore_backend.model.book.Book;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book; // 关联书籍

    @Column(name = "book_id", insertable = false, updatable = false)
    @JsonProperty("bookId")
    private Long bookId;

    @Column(nullable = false)
    private int number=1; // 购物车中的数量

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user; // 关联用户

    @Column(name = "user_id", insertable = false, updatable = false)
    @JsonProperty("userId")
    private Long userId;

    // Getters and Setters

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", book=" + book +
                ", number=" + number +
                ", user=" + user +
                ", userId=" + userId +
                '}';
    }
}

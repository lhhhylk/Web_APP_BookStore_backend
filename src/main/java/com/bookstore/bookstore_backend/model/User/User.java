package com.bookstore.bookstore_backend.model.User;

import com.bookstore.bookstore_backend.model.cart.CartItem;
import com.bookstore.bookstore_backend.model.order.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    private String avatar = "https://i-blog.csdnimg.cn/blog_migrate/a4fa5161369727154bc3a7d1c52bb9c0.png";

    private String tagLine = "";

    private Float balance = 1000.0f;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean disabled = false;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommonAddress> commonAddresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orders = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_auth_id", referencedColumnName = "id", nullable = false, unique = true)
    @JsonIgnore
    private UserAuth userAuth;

    // Getters and Setters
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public Float getBalance() {
        return balance;
    }

    public void setBalance(Float balance) {
        this.balance = balance;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<CommonAddress> getCommonAddresses() {
        return commonAddresses;
    }

    public void setCommonAddresses(List<CommonAddress> commonAddresses) {
        this.commonAddresses = commonAddresses;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public void addCartItem(CartItem cartItem) {
        this.cartItems.add(cartItem);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void addOrder(Order order) {
        this.orders.add(order);
    }

    public UserAuth getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(UserAuth userAuth) {
        this.userAuth = userAuth;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", tagLine='" + tagLine + '\'' +
                ", balance=" + balance +
                ", role=" + role +
                ", disabled=" + disabled +
                ", commonAddresses=" + commonAddresses +
                '}';
    }
}
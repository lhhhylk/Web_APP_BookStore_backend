package com.bookstore.bookstore_backend.model.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private String recipient;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> orderItems;
    private BigDecimal totalAmount;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<OrderItemDTO> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemDTO> orderItems) { this.orderItems = orderItems; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
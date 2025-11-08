package com.bookstore.bookstore_backend.model.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class OrderDTO {

    @NotBlank(message = "收货人不能为空")
    private String recipient;

    @NotBlank(message = "电话不能为空")
    private String phone;

    @NotBlank(message = "地址不能为空")
    private String address;

    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDTO> items;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<OrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDTO> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "OrderDTO{" +
                "recipient='" + recipient + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", items=" + items +
                '}';
    }
}
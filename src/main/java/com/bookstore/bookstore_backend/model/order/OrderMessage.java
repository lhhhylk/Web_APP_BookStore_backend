package com.bookstore.bookstore_backend.model.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
public class OrderMessage implements Serializable {
    private Long userId;
    private OrderDTO orderDTO;

    public OrderMessage(Long userId, OrderDTO orderDTO) {
        this.userId = userId;
        this.orderDTO = orderDTO;
    }
}
package com.bookstore.bookstore_backend.repository;

public interface UserConsumptionProjection {
    Long getUserId();
    String getUsername();
    String getEmail();
    Double getTotalConsumption();
}
package com.bookstore.bookstore_backend.model.User;

public class LoginResponseDTO {
    private Long userId;
    private String username;

    public LoginResponseDTO(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}

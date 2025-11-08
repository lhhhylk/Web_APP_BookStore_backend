package com.bookstore.bookstore_backend.model.User;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public class PasswordUpdateDTO {
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, message = "密码长度至少为6位")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
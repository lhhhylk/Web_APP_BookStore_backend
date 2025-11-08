package com.bookstore.bookstore_backend.model.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.Pattern;

public class UserDTO {
    private Long id;
//    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;
//    @Email(message = "邮箱格式错误")
    private String email;
//    @Pattern(regexp = "^(https?://.*\\.(?:png|jpg|jpeg|gif|svg))?$", message = "头像必须是有效的图片URL")
    private String avatar;
    private String tagLine;
    private Role role = Role.USER;
    private boolean disabled = false;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", avatar='" + avatar + '\'' +
                ", tagLine='" + tagLine + '\'' +
                ", role=" + role +
                '}';
    }
}
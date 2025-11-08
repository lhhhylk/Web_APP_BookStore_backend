package com.bookstore.bookstore_backend.model.comment;

import jakarta.validation.constraints.NotBlank;

public class CommentDTO {
    private Long id;

    @NotBlank(message = "评论不得为空")
    private String content; // 评论内容

    @NotBlank(message = "评论者不得为空")
    private String username; // 评论者

    private int likes;//点赞数

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "CommentDTO{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", username='" + username + '\'' +
                ", likes=" + likes +
                '}';
    }
}

package com.bookstore.bookstore_backend.model.comment;

import jakarta.validation.constraints.NotBlank;

public class CommentReplyDTO {
    private Long id;

    @NotBlank(message = "评论不得为空")
    private String content; // 回复内容
    @NotBlank(message = "评论者不得为空")
    private String username; // 回复者

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "CommentReplyDTO{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}

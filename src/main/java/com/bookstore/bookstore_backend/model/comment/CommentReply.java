package com.bookstore.bookstore_backend.model.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "comment_replies")
public class CommentReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 回复内容
    @Column(nullable = false)
    private String username; // 回复者

    @ManyToOne
    @JoinColumn(name = "comment_id")
    @JsonIgnore
    private Comment parentComment; // 关联的评论

    @Column(name = "comment_id", insertable = false, updatable = false)
    @JsonProperty("commentId")
    private Long commentId;

    // Getters and Setters

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

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    @Override
    public String toString() {
        return "CommentReply{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", username='" + username + '\'' +
                ", parentComment=" + parentComment +
                ", commentId=" + commentId +
                '}';
    }
}

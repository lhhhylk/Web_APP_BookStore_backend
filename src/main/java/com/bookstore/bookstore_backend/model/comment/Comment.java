package com.bookstore.bookstore_backend.model.comment;

import com.bookstore.bookstore_backend.model.book.Book;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content; // 评论内容

    @Column(nullable = false)
    private String username; // 评论者

    private int likes=0;//点赞数

    @ManyToOne
    @JoinColumn(name = "book_id")
    @JsonIgnore
    private Book book; // 关联书籍

    @Column(name = "book_id", insertable = false, updatable = false)
    @JsonProperty("bookId")
    private Long bookId;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentReply> replies; // 关联回复

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    // Getters and Setters

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<CommentReply> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentReply> replies) {
        this.replies = replies;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
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

    @Transient
    @JsonProperty("liked")
    private boolean liked;
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", username='" + username + '\'' +
                ", likes=" + likes +
                ", book=" + book +
                ", bookId=" + bookId +
                ", replies=" + replies +
                ", createdAt=" + createdAt +
                '}';
    }
}
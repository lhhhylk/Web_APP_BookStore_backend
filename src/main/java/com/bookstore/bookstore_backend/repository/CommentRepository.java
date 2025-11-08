package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBookId(Long bookId);
}

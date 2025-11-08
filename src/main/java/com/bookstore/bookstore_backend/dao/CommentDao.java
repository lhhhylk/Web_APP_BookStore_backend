package com.bookstore.bookstore_backend.dao;

import com.bookstore.bookstore_backend.model.comment.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentDao {
    List<Comment> findByBookId(Long bookId);
    Comment save(Comment comment);
    void deleteById(Long id);
    List<Comment> findAll();
    Optional<Comment> findById(Long id);
}
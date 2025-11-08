package com.bookstore.bookstore_backend.dao;

import com.bookstore.bookstore_backend.model.comment.CommentLike;
import java.util.Optional;

public interface CommentLikeDao {
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
    long countByCommentId(Long commentId);
    CommentLike save(CommentLike commentLike);
    void deleteById(Long id);
    Optional<CommentLike> findById(Long id);
}
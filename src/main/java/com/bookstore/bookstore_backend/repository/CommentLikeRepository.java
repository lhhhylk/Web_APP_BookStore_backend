package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.comment.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
    long countByCommentId(Long commentId);
}
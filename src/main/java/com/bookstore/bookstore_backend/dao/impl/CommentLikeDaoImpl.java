package com.bookstore.bookstore_backend.dao.impl;

import com.bookstore.bookstore_backend.dao.CommentLikeDao;
import com.bookstore.bookstore_backend.model.comment.CommentLike;
import com.bookstore.bookstore_backend.repository.CommentLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommentLikeDaoImpl implements CommentLikeDao {

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Override
    public Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId) {
        return commentLikeRepository.findByUserIdAndCommentId(userId, commentId);
    }

    @Override
    public void deleteByUserIdAndCommentId(Long userId, Long commentId) {
        commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
    }

    @Override
    public long countByCommentId(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

    @Override
    public CommentLike save(CommentLike commentLike) {
        return commentLikeRepository.save(commentLike);
    }

    @Override
    public void deleteById(Long id) {
        commentLikeRepository.deleteById(id);
    }

    @Override
    public Optional<CommentLike> findById(Long id) {
        return commentLikeRepository.findById(id);
    }
}
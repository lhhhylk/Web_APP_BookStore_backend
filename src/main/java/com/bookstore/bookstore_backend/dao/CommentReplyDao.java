package com.bookstore.bookstore_backend.dao;

import com.bookstore.bookstore_backend.model.comment.CommentReply;
import java.util.List;
import java.util.Optional;

public interface CommentReplyDao {
    List<CommentReply> findByParentCommentId(Long commentId);
    CommentReply save(CommentReply commentReply);
    void deleteById(Long id);
    Optional<CommentReply> findById(Long id);
}
package com.bookstore.bookstore_backend.services;

import com.bookstore.bookstore_backend.model.comment.Comment;
import com.bookstore.bookstore_backend.model.comment.CommentReply;
import com.bookstore.bookstore_backend.model.comment.CommentReplyDTO;

import java.util.List;
import java.util.Map;

public interface ICommentService {
    Comment getCommentById(Long commentId);
    int getLikesByCommentId(Long commentId);
    CommentReply addReplyToComment(Long commentId, CommentReplyDTO replyDTO);
    List<CommentReply> getRepliesByCommentId(Long commentId);
    Map<String, Object> likeCommentById(Long id, Long userId);
    Map<String, Object> unlikeCommentById(Long id, Long userId);
    List<Comment> getCommentsByBookId(Long bookId, Long userId);
}
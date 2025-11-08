package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.comment.CommentReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentReplyRepository extends JpaRepository<CommentReply, Long> {
    List<CommentReply> findByParentCommentId(Long commentId);
}

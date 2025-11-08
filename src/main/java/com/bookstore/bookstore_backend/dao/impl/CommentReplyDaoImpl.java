package com.bookstore.bookstore_backend.dao.impl;

import com.bookstore.bookstore_backend.dao.CommentReplyDao;
import com.bookstore.bookstore_backend.model.comment.CommentReply;
import com.bookstore.bookstore_backend.repository.CommentReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommentReplyDaoImpl implements CommentReplyDao {

    @Autowired
    private CommentReplyRepository commentReplyRepository;

    @Override
    public List<CommentReply> findByParentCommentId(Long commentId) {
        return commentReplyRepository.findByParentCommentId(commentId);
    }

    @Override
    public CommentReply save(CommentReply commentReply) {
        return commentReplyRepository.save(commentReply);
    }

    @Override
    public void deleteById(Long id) {
        commentReplyRepository.deleteById(id);
    }

    @Override
    public Optional<CommentReply> findById(Long id) {
        return commentReplyRepository.findById(id);
    }
}
package com.bookstore.bookstore_backend.dao.impl;

import com.bookstore.bookstore_backend.dao.CommentDao;
import com.bookstore.bookstore_backend.model.comment.Comment;
import com.bookstore.bookstore_backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommentDaoImpl implements CommentDao {

    @Autowired
    private CommentRepository commentRepository;

    @Override
    public List<Comment> findByBookId(Long bookId) {
        return commentRepository.findByBookId(bookId);
    }

    @Override
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }
}
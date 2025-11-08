package com.bookstore.bookstore_backend.services.Impl;

import com.bookstore.bookstore_backend.model.comment.Comment;
import com.bookstore.bookstore_backend.model.comment.CommentLike;
import com.bookstore.bookstore_backend.model.comment.CommentReply;
import com.bookstore.bookstore_backend.model.comment.CommentReplyDTO;
import com.bookstore.bookstore_backend.repository.CommentLikeRepository;
import com.bookstore.bookstore_backend.repository.CommentReplyRepository;
import com.bookstore.bookstore_backend.repository.CommentRepository;
import com.bookstore.bookstore_backend.repository.CommonAddressRepository;
import com.bookstore.bookstore_backend.services.ICommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService implements ICommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentReplyRepository commentReplyRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommonAddressRepository commonAddressRepository;

    @Override
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在，参数异常"));
    }

    @Override
    public int getLikesByCommentId(Long commentId) {
        return (int) commentLikeRepository.countByCommentId(commentId);
    }

    @Override
    public CommentReply addReplyToComment(Long commentId, CommentReplyDTO replyDTO) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        CommentReply reply = new CommentReply();
        BeanUtils.copyProperties(replyDTO, reply);
        reply.setParentComment(comment);
        return commentReplyRepository.save(reply);
    }

    @Override
    public List<CommentReply> getRepliesByCommentId(Long commentId) {
        return commentReplyRepository.findByParentCommentId(commentId);
    }

    @Override
    @Transactional
    public Map<String, Object> likeCommentById(Long id, Long userId) {
        Comment comment = getCommentById(id);
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(userId, id);
        Map<String, Object> response = new HashMap<>();

        if (existingLike.isPresent()) {
            response.put("code", 400);
            response.put("message", "您已点赞该评论");
            return response;
        }

        CommentLike like = new CommentLike();
        like.setUserId(userId);
        like.setComment(comment);
        commentLikeRepository.save(like);

        long likes = commentLikeRepository.countByCommentId(id);
        response.put("code", 200);
        response.put("likes", likes);
        response.put("liked", true);
        response.put("message", "点赞成功");
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> unlikeCommentById(Long id, Long userId) {
        Comment comment = getCommentById(id);
        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(userId, id);
        Map<String, Object> response = new HashMap<>();

        if (!existingLike.isPresent()) {
            response.put("code", 400);
            response.put("message", "您未点赞该评论");
            return response;
        }

        commentLikeRepository.deleteByUserIdAndCommentId(userId, id);
        long likes = commentLikeRepository.countByCommentId(id);
        response.put("code", 200);
        response.put("likes", likes);
        response.put("liked", false);
        response.put("message", "取消点赞成功");
        return response;
    }

    @Override
    public List<Comment> getCommentsByBookId(Long bookId, Long userId) {
        List<Comment> comments = commentRepository.findByBookId(bookId);
        return comments.stream().map(comment -> {
            comment.setLiked(commentLikeRepository.findByUserIdAndCommentId(userId, comment.getId()).isPresent());
            return comment;
        }).collect(Collectors.toList());
    }
}
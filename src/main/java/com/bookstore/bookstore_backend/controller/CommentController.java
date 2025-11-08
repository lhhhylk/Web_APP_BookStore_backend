package com.bookstore.bookstore_backend.controller;

import com.bookstore.bookstore_backend.model.ResponseMessage;
import com.bookstore.bookstore_backend.model.comment.Comment;
import com.bookstore.bookstore_backend.model.comment.CommentReply;
import com.bookstore.bookstore_backend.model.comment.CommentReplyDTO;
import com.bookstore.bookstore_backend.services.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @PutMapping("/{id}/like")
    public ResponseMessage<Map<String, Object>> like(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null) {
            return ResponseMessage.error("userId 不能为空");
        }
        Map<String, Object> result = commentService.likeCommentById(id, userId);
        return ResponseMessage.success(result);
    }

    @PutMapping("/{id}/unlike")
    public ResponseMessage<Map<String, Object>> unlike(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        if (userId == null) {
            return ResponseMessage.error("userId 不能为空");
        }
        Map<String, Object> result = commentService.unlikeCommentById(id, userId);
        return ResponseMessage.success(result);
    }

    @GetMapping("/{id}/reply")
    public ResponseMessage<List<CommentReply>> getReplyByCommentId(@PathVariable Long id) {
        List<CommentReply> replies = commentService.getRepliesByCommentId(id);
        return ResponseMessage.success(replies);
    }

    @PostMapping("/add/{id}")
    public ResponseMessage<CommentReply> addReplytoComment(@PathVariable Long id, @RequestBody CommentReplyDTO commentReply) {
        CommentReply reply = commentService.addReplyToComment(id, commentReply);
        return ResponseMessage.success(reply);
    }

    @GetMapping("/book/{bookId}")
    public ResponseMessage<List<Comment>> getCommentsByBookId(@PathVariable Long bookId, @RequestParam(required = false) Long userId) {
        List<Comment> comments = commentService.getCommentsByBookId(bookId, userId != null ? userId : 0L);
        return ResponseMessage.success(comments);
    }
}
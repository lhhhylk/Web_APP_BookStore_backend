package com.bookstore.bookstore_backend.controller;

import com.bookstore.bookstore_backend.model.ResponseMessage;
import com.bookstore.bookstore_backend.model.book.*;
import com.bookstore.bookstore_backend.model.comment.Comment;
import com.bookstore.bookstore_backend.model.comment.CommentDTO;
import com.bookstore.bookstore_backend.services.IBookService;
import com.bookstore.bookstore_backend.services.ITagService;
import com.bookstore.bookstore_backend.services.AuthorServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/books")
public class BookController {
    @Autowired
    private IBookService bookService;

    @Autowired
    private ITagService tagService;

    @Autowired
    private AuthorServiceClient authorServiceClient;

    @GetMapping("/find")
    public ResponseMessage<List<Book>> getBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "5") int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Book> bookPage = bookService.getBooks(keyword, tag, pageable);
        return ResponseMessage.success(bookPage.getContent()).setTotal(bookPage.getTotalElements());
    }

    @GetMapping("/find/{id}")
    public ResponseMessage<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ResponseMessage.success(book);
    }

    @GetMapping("/get/{id}/comments")
    public ResponseMessage<List<Comment>> getComments(@PathVariable Long id) {
        List<Comment> comments = bookService.getCommentsByBookId(id);
        return ResponseMessage.success(comments);
    }

    @GetMapping("/tags")
    public ResponseMessage<Set<String>> getAllTags() {
        return ResponseMessage.success(bookService.getAllTags());
    }

    //TODO 库存保存有问题
    @PostMapping("/save")
    public ResponseMessage<Book> addBook(@RequestBody Book book) {
        Book savedBook = bookService.saveBook(book);
        return ResponseMessage.success(savedBook);
    }

    @PostMapping("/add/{id}/comments")
    public ResponseMessage<Comment> addComment(@PathVariable Long id, @RequestBody CommentDTO commentDTO) {
        Comment newComment = bookService.addCommentToBook(id, commentDTO);
        return ResponseMessage.success(newComment);
    }

    @PutMapping("/update/{id}")
    public ResponseMessage<Book> updateBook(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        Book updatedBook = bookService.updateBook(id, bookDTO);
        return ResponseMessage.success(updatedBook);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseMessage deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseMessage.success(null);
    }

    @GetMapping("/salesRank")
    public ResponseMessage<List<Book>> getBooksSalesRank() {
        List<Book> books = bookService.getBooksOrderBySalesDesc();
        return ResponseMessage.success(books);
    }

    /**
     * 基于标签图搜索图书
     * 根据用户选中的标签，在Neo4j中查找通过2跳可以关联到的所有标签，
     * 然后在MySQL中搜索所有带有这些标签中任意一个或多个的图书
     * @param keyword 关键词（可选）
     * @param tag 用户选中的标签
     * @param pageIndex 页码（从0开始）
     * @param pageSize 每页大小
     * @return 图书列表
     */
    @GetMapping("/search-by-tag-graph")
    public ResponseMessage<List<Book>> searchBooksByTagGraph(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "5") int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Book> bookPage = bookService.getBooksByTagGraph(keyword, tag, pageable);
        return ResponseMessage.success(bookPage.getContent()).setTotal(bookPage.getTotalElements());
    }

    /**
     * 基于标签图搜索图书（支持多个标签）
     * @param keyword 关键词（可选）
     * @param tags 用户选中的标签列表（逗号分隔）
     * @param pageIndex 页码（从0开始）
     * @param pageSize 每页大小
     * @return 图书列表
     */
    @GetMapping("/search-by-tag-graph-multiple")
    public ResponseMessage<List<Book>> searchBooksByTagGraphMultiple(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tags,  // 逗号分隔的标签字符串
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "5") int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        List<String> tagList = null;
        if (tags != null && !tags.trim().isEmpty()) {
            tagList = java.util.Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
        }
        Page<Book> bookPage = bookService.getBooksByTagGraph(keyword, tagList, pageable);
        return ResponseMessage.success(bookPage.getContent()).setTotal(bookPage.getTotalElements());
    }

    /**
     * 构建标签图
     * 从MySQL中获取所有标签，分析标签之间的层次关系，并构建到Neo4j中
     * @return 操作结果
     */
    @PostMapping("/build-tag-graph")
    public ResponseMessage<String> buildTagGraph() {
        try {
            tagService.buildTagGraph();
            return ResponseMessage.success("标签图构建成功");
        } catch (Exception e) {
            return ResponseMessage.error("标签图构建失败: " + e.getMessage());
        }
    }

    /**
     * 清空标签图
     * @return 操作结果
     */
    @DeleteMapping("/clear-tag-graph")
    public ResponseMessage<String> clearTagGraph() {
        try {
            tagService.clearAllTags();
            return ResponseMessage.success("标签图已清空");
        } catch (Exception e) {
            return ResponseMessage.error("清空标签图失败: " + e.getMessage());
        }
    }

    /**
     * 建立标签包含关系：使标签a包含标签b（即b是a的子标签）
     * @param parentTag 父标签（标签a）
     * @param childTag 子标签（标签b）
     * @return 操作结果
     */
    @PostMapping("/tags/relationship")
    public ResponseMessage<String> addTagRelationship(
            @RequestParam String parentTag,
            @RequestParam String childTag) {
        try {
            tagService.addTagRelationship(parentTag, childTag);
            return ResponseMessage.success(String.format("成功建立关系：%s 包含 %s", parentTag, childTag));
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(e.getMessage());
        } catch (Exception e) {
            return ResponseMessage.error("建立标签关系失败: " + e.getMessage());
        }
    }

    /**
     * 根据书名搜索作者（通过Author Service微服务）
     * @param title 书名
     * @return 作者信息
     */
    @GetMapping("/search-author")
    public ResponseMessage<AuthorResponse> searchAuthorByTitle(@RequestParam String title) {
        try {
            AuthorResponse authorResponse = authorServiceClient.getAuthorByTitle(title);
            if (authorResponse != null && authorResponse.isSuccess()) {
                return ResponseMessage.success(authorResponse);
            } else {
                return ResponseMessage.error(authorResponse != null ? authorResponse.getMessage() : "未找到该书的作者信息");
            }
        } catch (Exception e) {
            return ResponseMessage.error("调用Author Service失败: " + e.getMessage());
        }
    }
}
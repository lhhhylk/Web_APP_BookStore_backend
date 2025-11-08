package com.bookstore.bookstore_backend.services.Impl;

import com.bookstore.bookstore_backend.model.book.Book;
import com.bookstore.bookstore_backend.model.book.BookDTO;
import com.bookstore.bookstore_backend.model.book.BookStock;
import com.bookstore.bookstore_backend.model.comment.Comment;
import com.bookstore.bookstore_backend.model.comment.CommentDTO;
import com.bookstore.bookstore_backend.repository.BookRepository;
import com.bookstore.bookstore_backend.repository.CommentRepository;
import com.bookstore.bookstore_backend.repository.BookStockRepository;
import com.bookstore.bookstore_backend.services.IBookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookService implements IBookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private static final String BOOK_CACHE_PREFIX = "book:";
    private static final long CACHE_TTL_SECONDS = 3600; // 1 hour TTL

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookStockRepository bookStockRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate; // 新增：RedisTemplate注入

    private final ObjectMapper objectMapper = new ObjectMapper(); // 用于序列化/反序列化

    private void loadInventory(Book book) {
        if (book.getId() != null) {
            bookStockRepository.findByBookId(book.getId()).ifPresent(stock ->
                    book.setInventory(stock.getInventory())
            );
        }
    }

    // **新增：从Redis获取书籍的辅助方法**
    private Book getBookFromCache(Long id) {
        try {
            String key = BOOK_CACHE_PREFIX + id;
            Book cached = (Book) redisTemplate.opsForValue().get(key);
            if (cached != null) {
                logger.info("Cache hit for book id: {}", id);
                // **新增：反序列化后，Transient字段需重新加载（但inventory在put时已设）**
                loadInventory(cached);  // 保险起见，重新合并库存（若TTL内不变）
                return cached;
            }
        } catch (Exception e) {
            logger.warn("Failed to get book {} from cache: {}", id, e.getMessage());
        }
        logger.info("Cache miss for book id: {}", id);
        return null;
    }

    // **新增：将书籍存入Redis的辅助方法**
    private void putBookToCache(Book book) {
        try {
            // **新增：手动初始化懒加载字段，确保完整序列化**
            Hibernate.initialize(book.getTags());  // 初始化tags
            Hibernate.initialize(book.getComments());  // 初始化comments（即使@JsonIgnore，也防潜在问题）

            // **新增：转换Hibernate包装的集合为普通集合，避免反序列化时触发懒加载**
            // 这确保JSON中tags是java.util.ArrayList，而非org.hibernate.collection.internal.PersistentBag
            if (book.getTags() != null) {
                book.setTags(new ArrayList<>(book.getTags()));
            }
            // 如果有其他集合（如comments不忽略时），类似处理：book.setComments(new ArrayList<>(book.getComments()));

            // inventory已在loadInventory时设，无需init

            String key = BOOK_CACHE_PREFIX + book.getId();
            redisTemplate.opsForValue().set(key, book, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            logger.info("Updated cache for book id: {} (with plain collections)", book.getId());
        } catch (Exception e) {
            logger.warn("Failed to put book {} to cache: {}", book.getId(), e.getMessage());
        }
    }

    // **新增：删除书籍缓存的辅助方法**
    private void evictBookFromCache(Long id) {
        try {
            String key = BOOK_CACHE_PREFIX + id;
            redisTemplate.delete(key);
            logger.info("Evicted cache for book id: {}", id);
        } catch (Exception e) {
            logger.warn("Failed to evict book {} from cache: {}", id, e.getMessage());
        }
    }

    @Override
    public List<Book> getAllBooks() {
        List<Book> books = bookRepository.findAllByDeletedFalse();
        // **新增：为每个书籍尝试从缓存加载完整信息（包括inventory）**
        books.forEach(book -> {
            Book cachedBook = getBookFromCache(book.getId());
            if (cachedBook != null) {
                // 用缓存覆盖基本信息
                BeanUtils.copyProperties(cachedBook, book, "comments"); // 避免覆盖comments
            } else {
                loadInventory(book);
                putBookToCache(book); // 缓存完整书籍
            }
        });
        return books;
    }

    @Override
    public Page<Book> getBooks(String keyword, String tag, Pageable pageable) {
        Page<Book> page = bookRepository.findBooksByKeywordAndTagWithPaginationAndNotDeleted(keyword, tag, pageable);
        // **新增：为每页书籍尝试从缓存加载完整信息**
        page.getContent().forEach(book -> {
            Book cachedBook = getBookFromCache(book.getId());
            if (cachedBook != null) {
                BeanUtils.copyProperties(cachedBook, book, "comments");
            } else {
                loadInventory(book);
                putBookToCache(book);
            }
        });
        return page;
    }

    @Override
    public long countBooks(String keyword, String tag) {
        return bookRepository.countBooksByKeywordAndTagAndNotDeleted(keyword, tag);
    }

    @Override
    public Book getBookById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("书籍ID不能为空");
        }
        // **变更：先查缓存**
        Book book = getBookFromCache(id);
        if (book == null) {
            book = bookRepository.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new IllegalArgumentException("书籍不存在或已被删除"));
            loadInventory(book);
            putBookToCache(book); // 缓存完整书籍
        }
        return book;
    }

    //TODO：库存管理需要更加合理
    @Override
    public Book saveBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("书籍信息不能为空");
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("书籍标题不能为空");
        }
        Book savedBook = bookRepository.save(book);
        // **修改：保存后创建库存记录（默认100）**
        BookStock stock = new BookStock();
        stock.setBookId(savedBook.getId());  // 修正：使用savedBook.getId()
        // **新增：如果用户设置了有效库存，则使用它；否则默认100**
        if (book.getInventory() > 0) {
            stock.setInventory(book.getInventory());
        } else {
            stock.setInventory(100);  // 默认值
        }
        bookStockRepository.save(stock);
        // **加载到 Transient**
        loadInventory(savedBook);
        // **新增：缓存新书籍**
        putBookToCache(savedBook);
        logger.info("New book saved and cached: id={}", savedBook.getId());
        return savedBook;
    }

    @Override
    public void deleteBook(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("书籍ID不能为空");
        }
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("书籍不存在，参数异常"));
        book.setDeleted(true);
        bookRepository.save(book);
        // **新增：删除缓存**
        evictBookFromCache(id);
        logger.info("Book deleted (soft) and cache evicted: id={}", id);
    }

    @Override
    public Comment addCommentToBook(Long bookId, CommentDTO commentDTO) {
        if (bookId == null) {
            throw new IllegalArgumentException("书籍ID不能为空");
        }
        if (commentDTO == null) {
            throw new IllegalArgumentException("评论信息不能为空");
        }
        if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        Book book = bookRepository.findByIdAndDeletedFalse(bookId)
                .orElseThrow(() -> new IllegalArgumentException("书籍不存在或已被删除"));
        Comment newComment = new Comment();
        BeanUtils.copyProperties(commentDTO, newComment);
        newComment.setBook(book);
        Comment savedComment = commentRepository.save(newComment);
        // **新增：评论更新后，失效书籍缓存（因为comments可能影响Book，但这里暂不缓存comments）**
        evictBookFromCache(bookId);
        logger.info("Comment added to book id={}, cache evicted", bookId);
        return savedComment;
    }

    @Override
    public List<Comment> getCommentsByBookId(Long bookId) {
        return commentRepository.findByBookId(bookId);
    }

    @Override
    public Set<String> getAllTags() {
        // **新增：标签列表也缓存（简单字符串集合）**
        String tagsKey = "all_tags";
        try {
            Set<String> cachedTags = (Set<String>) redisTemplate.opsForValue().get(tagsKey);
            if (cachedTags != null) {
                logger.info("Cache hit for all tags");
                return cachedTags;
            }
        } catch (Exception e) {
            logger.warn("Failed to get all tags from cache: {}", e.getMessage());
        }
        List<List<String>> tagsList = bookRepository.findAllTagsByDeletedFalse();
        Set<String> tags = tagsList.stream().flatMap(List::stream).collect(Collectors.toSet());
        try {
            redisTemplate.opsForValue().set(tagsKey, tags, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            logger.info("Cached all tags");
        } catch (Exception e) {
            logger.warn("Failed to cache all tags: {}", e.getMessage());
        }
        return tags;
    }

    @Override
    public Book updateBook(Long id, BookDTO bookDTO) {
        if (id == null) {
            throw new IllegalArgumentException("书籍ID不能为空");
        }
        if (bookDTO == null) {
            throw new IllegalArgumentException("书籍信息不能为空");
        }
        if (bookDTO.getTitle() == null || bookDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("书籍标题不能为空");
        }
        Book existingBook = bookRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("书籍不存在或已被删除"));
        BeanUtils.copyProperties(bookDTO, existingBook, "id", "comments");
        // **新增：若 DTO 有 inventory，更新库存**
        // 假设 BookDTO 有 getInventory()
        BookStock stock = bookStockRepository.findByBookId(id)
                .orElseThrow(() -> new IllegalArgumentException("库存记录不存在"));
        stock.setInventory(bookDTO.getInventory());
        bookStockRepository.save(stock);

        Book updated = bookRepository.save(existingBook);
        // **加载最新库存**
        loadInventory(updated);
        // **新增：更新后重新缓存**
        putBookToCache(updated);
        logger.info("Book updated and cache refreshed: id={}", id);
        return updated;
    }

    @Override
    public List<Book> getBooksOrderBySalesDesc() {
        // **新增：销售排行也从缓存加载（但由于动态，可能不缓存整个列表；这里为每个书缓存）**
        List<Book> books = bookRepository.findBooksOrderBySalesDesc();
        books.forEach(book -> {
            Book cachedBook = getBookFromCache(book.getId());
            if (cachedBook != null) {
                BeanUtils.copyProperties(cachedBook, book, "comments");
            } else {
                loadInventory(book);
                putBookToCache(book);
            }
        });
        return books;
    }
}
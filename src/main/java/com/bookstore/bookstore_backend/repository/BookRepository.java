package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    long count();

    @Query("SELECT DISTINCT tags FROM Book WHERE deleted = false")
    List<List<String>> findAllTagsByDeletedFalse();

    @Query("SELECT b FROM Book b WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:tag IS NULL OR :tag MEMBER OF b.tags) AND b.deleted = false")
    Page<Book> findBooksByKeywordAndTagWithPaginationAndNotDeleted(@Param("keyword") String keyword, @Param("tag") String tag, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:tag IS NULL OR :tag MEMBER OF b.tags) AND b.deleted = false")
    List<Book> findBooksByKeywordAndTagAndNotDeleted(@Param("keyword") String keyword, @Param("tag") String tag);

    @Query("SELECT COUNT(b) FROM Book b WHERE (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:tag IS NULL OR :tag MEMBER OF b.tags) AND b.deleted = false")
    long countBooksByKeywordAndTagAndNotDeleted(@Param("keyword") String keyword, @Param("tag") String tag);

    @Query("SELECT b FROM Book b WHERE b.id = :id AND b.deleted = false")
    Optional<Book> findByIdAndDeletedFalse(@Param("id") Long id);

    @Query("SELECT b FROM Book b WHERE b.deleted = false")
    List<Book> findAllByDeletedFalse();

    @Query("SELECT b FROM Book b WHERE b.deleted = false ORDER BY b.sales DESC")
    List<Book> findBooksOrderBySalesDesc();

    /**
     * 根据关键词和多个标签搜索图书（标签之间是OR关系）
     * @param keyword 关键词（可为null）
     * @param tags 标签列表
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT DISTINCT b FROM Book b WHERE " +
           "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:tags IS NULL OR EXISTS(SELECT t FROM b.tags t WHERE t IN :tags)) " +
           "AND b.deleted = false")
    Page<Book> findBooksByKeywordAndTagsWithPaginationAndNotDeleted(@Param("keyword") String keyword, @Param("tags") List<String> tags, Pageable pageable);

    /**
     * 根据关键词和多个标签搜索图书（不分页）
     */
    @Query("SELECT DISTINCT b FROM Book b WHERE " +
           "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:tags IS NULL OR EXISTS(SELECT t FROM b.tags t WHERE t IN :tags)) " +
           "AND b.deleted = false")
    List<Book> findBooksByKeywordAndTagsAndNotDeleted(@Param("keyword") String keyword, @Param("tags") List<String> tags);

    /**
     * 统计符合关键词和多个标签的图书数量
     */
    @Query("SELECT COUNT(DISTINCT b) FROM Book b WHERE " +
           "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:tags IS NULL OR EXISTS(SELECT t FROM b.tags t WHERE t IN :tags)) " +
           "AND b.deleted = false")
    long countBooksByKeywordAndTagsAndNotDeleted(@Param("keyword") String keyword, @Param("tags") List<String> tags);
}
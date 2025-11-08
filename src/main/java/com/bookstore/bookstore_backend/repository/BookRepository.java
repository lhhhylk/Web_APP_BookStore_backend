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
}
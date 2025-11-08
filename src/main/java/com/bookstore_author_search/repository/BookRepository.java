package com.bookstore_author_search.repository;

import com.bookstore_author_search.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 书籍Repository
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * 根据书名查询书籍（不包含已删除的）
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) = LOWER(:title) AND b.deleted = false")
    Optional<Book> findByTitleIgnoreCaseAndNotDeleted(@Param("title") String title);

    /**
     * 根据书名模糊查询（不包含已删除的）
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) AND b.deleted = false")
    Optional<Book> findByTitleContainingIgnoreCaseAndNotDeleted(@Param("title") String title);
}

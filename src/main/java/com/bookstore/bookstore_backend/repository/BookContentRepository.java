package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.book.BookContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookContentRepository extends MongoRepository<BookContent, Long> {
    @Query("{ '$or': [ { '_id': ?0 }, { 'bookId': ?0 }, { '_id': ?1 } ] }")
    Optional<BookContent> findByBookIdOrLegacyId(Long numericId, String numericIdAsString);
}


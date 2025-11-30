package com.bookstore.bookstore_backend.migration;

import com.bookstore.bookstore_backend.model.book.BookContent;
import com.bookstore.bookstore_backend.repository.BookContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BookContentMigrationRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(BookContentMigrationRunner.class);
    private static final String SELECT_BOOK_CONTENT_SQL =
            """
            SELECT id, cover, description
            FROM books
            WHERE (cover IS NOT NULL AND cover <> '')
               OR (description IS NOT NULL AND description <> '')
            """;

    private final JdbcTemplate jdbcTemplate;
    private final BookContentRepository bookContentRepository;
    private final boolean migrationEnabled;
    private final int chunkSize;

    public BookContentMigrationRunner(JdbcTemplate jdbcTemplate,
                                      BookContentRepository bookContentRepository,
                                      @Value("${migration.book-content.enabled:false}") boolean migrationEnabled,
                                      @Value("${migration.book-content.chunk-size:200}") int chunkSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookContentRepository = bookContentRepository;
        this.migrationEnabled = migrationEnabled;
        this.chunkSize = chunkSize;
    }

    @Override
    public void run(String... args) {
        if (!migrationEnabled) {
            logger.info("Book content migration disabled. Set migration.book-content.enabled=true to run.");
            return;
        }
        logger.info("Starting book content migration from MySQL to MongoDB...");
        List<BookContent> buffer = new ArrayList<>(chunkSize);
        AtomicInteger migrated = new AtomicInteger(0);
        jdbcTemplate.query(SELECT_BOOK_CONTENT_SQL, rs -> {
            buffer.add(mapToContent(rs));
            if (buffer.size() >= chunkSize) {
                migrated.addAndGet(saveBuffer(buffer));
            }
        });
        if (!buffer.isEmpty()) {
            migrated.addAndGet(saveBuffer(buffer));
        }
        logger.info("Book content migration completed. Total migrated records: {}", migrated.get());
    }

    private int saveBuffer(List<BookContent> buffer) {
        bookContentRepository.saveAll(buffer);
        int size = buffer.size();
        buffer.clear();
        return size;
    }

    private BookContent mapToContent(ResultSet rs) throws SQLException {
        BookContent content = new BookContent();
        content.setBookId(rs.getLong("id"));
        content.setCover(rs.getString("cover"));
        content.setDescription(rs.getString("description"));
        return content;
    }
}


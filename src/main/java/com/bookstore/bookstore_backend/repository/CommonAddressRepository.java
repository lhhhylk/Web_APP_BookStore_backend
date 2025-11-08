package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.User.CommonAddress;
import com.bookstore.bookstore_backend.model.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonAddressRepository extends JpaRepository<CommonAddress, Long> {
    List<CommonAddress> findByUserId(Long userId);
}

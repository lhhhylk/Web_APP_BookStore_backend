package com.bookstore.bookstore_backend.repository;

import com.bookstore.bookstore_backend.model.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthRepository extends JpaRepository<User, Long> {
}

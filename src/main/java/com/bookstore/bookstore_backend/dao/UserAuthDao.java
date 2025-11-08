package com.bookstore.bookstore_backend.dao;

import com.bookstore.bookstore_backend.model.User.User;
import java.util.Optional;

public interface UserAuthDao {
    User save(User user);
    void deleteById(Long id);
    Optional<User> findById(Long id);
}
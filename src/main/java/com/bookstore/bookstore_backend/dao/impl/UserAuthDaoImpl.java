package com.bookstore.bookstore_backend.dao.impl;

import com.bookstore.bookstore_backend.dao.UserAuthDao;
import com.bookstore.bookstore_backend.model.User.User;
import com.bookstore.bookstore_backend.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserAuthDaoImpl implements UserAuthDao {

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Override
    public User save(User user) {
        return userAuthRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        userAuthRepository.deleteById(id);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userAuthRepository.findById(id);
    }
}
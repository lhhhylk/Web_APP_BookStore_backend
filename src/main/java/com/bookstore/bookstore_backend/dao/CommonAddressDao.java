package com.bookstore.bookstore_backend.dao;

import com.bookstore.bookstore_backend.model.User.CommonAddress;
import java.util.List;
import java.util.Optional;

public interface CommonAddressDao {
    List<CommonAddress> findByUserId(Long userId);
    CommonAddress save(CommonAddress address);
    void deleteById(Long id);
    Optional<CommonAddress> findById(Long id);
}
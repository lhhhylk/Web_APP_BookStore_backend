package com.bookstore.bookstore_backend.dao.impl;

import com.bookstore.bookstore_backend.dao.CommonAddressDao;
import com.bookstore.bookstore_backend.model.User.CommonAddress;
import com.bookstore.bookstore_backend.repository.CommonAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CommonAddressDaoImpl implements CommonAddressDao {

    @Autowired
    private CommonAddressRepository commonAddressRepository;

    @Override
    public List<CommonAddress> findByUserId(Long userId) {
        return commonAddressRepository.findByUserId(userId);
    }

    @Override
    public CommonAddress save(CommonAddress address) {
        return commonAddressRepository.save(address);
    }

    @Override
    public void deleteById(Long id) {
        commonAddressRepository.deleteById(id);
    }

    @Override
    public Optional<CommonAddress> findById(Long id) {
        return commonAddressRepository.findById(id);
    }
}
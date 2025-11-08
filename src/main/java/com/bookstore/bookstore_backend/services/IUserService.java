package com.bookstore.bookstore_backend.services;

import com.bookstore.bookstore_backend.model.User.*;
import com.bookstore.bookstore_backend.repository.UserConsumptionProjection;

import java.util.List;

public interface IUserService {
    User registerUser(UserDTO userDTO);
    User getUserByUsername(String username);
    User getUserById(Long id);
    User updateUser(UserDTO userDTO);
    void deleteUser(Long id);
    User updatePassword(Long userId, String newPassword);
    User updateAvatar(Long userId, String avatar);
    User updateTagLine(Long userId, String tagLine);
    User addCommonAddress(Long userId, CommonAddress address);
    List<CommonAddress> getCommonAddresses(Long userId);
    LoginResponseDTO login(String username, String password);
    void deleteAddress(Long userId, Long addressId);
    User disableUser(Long userId);
    User enableUser(Long userId);
    List<User> getAllUsers();
    List<UserConsumptionProjection> getUsersConsumptionRanking();
}
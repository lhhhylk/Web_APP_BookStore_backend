package com.bookstore.bookstore_backend.services.Impl;

import com.bookstore.bookstore_backend.model.User.*;
import com.bookstore.bookstore_backend.repository.CommonAddressRepository;
import com.bookstore.bookstore_backend.repository.UserAuthRepository;
import com.bookstore.bookstore_backend.repository.UserRepository;
import com.bookstore.bookstore_backend.services.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.bookstore.bookstore_backend.repository.UserConsumptionProjection;

import java.util.List;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAuthRepository userAuthRepository;

    @Autowired
    private CommonAddressRepository commonAddressRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user, "password");

        UserAuth userAuth = new UserAuth();
        userAuth.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        System.out.println("Encoded password: " + userAuth.getPassword());
        userAuth.setUser(user);
        user.setUserAuth(userAuth);

        return userRepository.save(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在，参数异常"));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在，参数异常"));
    }

    @Override
    public User updateUser(UserDTO userDTO) {
        User user = getUserById(userDTO.getId());
        BeanUtils.copyProperties(userDTO, user, "password");
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User updatePassword(Long userId, String newPassword) {
        User user = getUserById(userId);
        UserAuth userAuth = user.getUserAuth();
        if (userAuth == null) {
            userAuth = new UserAuth();
            user.setUserAuth(userAuth);
            userAuth.setUser(user);
        }
        userAuth.setPassword(passwordEncoder.encode(newPassword));
        user.setUserAuth(userAuth);
        return userRepository.save(user);
    }

    @Override
    public User updateAvatar(Long userId, String avatar) {
        User user = getUserById(userId);
        if (avatar == null || !avatar.matches("^(https?://.*\\.(?:png|jpg|jpeg|gif|svg))?$")) {
            throw new IllegalArgumentException("头像必须是有效的图片URL");
        }
        user.setAvatar(avatar);
        return userRepository.save(user);
    }

    @Override
    public User updateTagLine(Long userId, String tagLine) {
        User user = getUserById(userId);
        if (tagLine == null) {
            tagLine = "";
        }
        user.setTagLine(tagLine);
        return userRepository.save(user);
    }

    @Override
    public User addCommonAddress(Long userId, CommonAddress address) {
        User user = getUserById(userId);
        if (address.getRecipient() == null || address.getPhone() == null || address.getAddress() == null) {
            throw new IllegalArgumentException("收货人、电话和地址不能为空");
        }
        address.setUser(user);
        user.getCommonAddresses().add(address);
        return userRepository.save(user);
    }

    @Override
    public List<CommonAddress> getCommonAddresses(Long userId) {
        User user = getUserById(userId);
        return user.getCommonAddresses();
    }

    @Override
    public LoginResponseDTO login(String username, String password) {
        System.out.println("Attempting login for username: " + username);
        System.out.println("Raw password: " + password);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户名错误->用户名或密码错误"));

        if (user.isDisabled()) {
            throw new IllegalArgumentException("用户已被禁用，无法登录");
        }

        UserAuth userAuth = user.getUserAuth();
        System.out.println("Stored hashed password: " + (userAuth != null ? userAuth.getPassword() : "null"));
        System.out.println("Raw password: " + passwordEncoder.encode(password));
        if (userAuth == null || !passwordEncoder.matches(password, userAuth.getPassword())) {
            throw new IllegalArgumentException("密码错误->用户名或密码错误");
        }

        return new LoginResponseDTO(user);
    }

    @Override
    public void deleteAddress(Long userId, Long addressId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户id错误"));
        CommonAddress address = commonAddressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("地址id错误"));
        user.getCommonAddresses().remove(address);
        userRepository.save(user);
    }

    @Override
    public User disableUser(Long userId) {
        User user = getUserById(userId);
        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("不能禁用管理员用户");
        }
        user.setDisabled(true);
        return userRepository.save(user);
    }

    @Override
    public User enableUser(Long userId) {
        User user = getUserById(userId);
        user.setDisabled(false);
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<UserConsumptionProjection> getUsersConsumptionRanking() {
        return userRepository.findUserConsumptionRanking();
    }
}
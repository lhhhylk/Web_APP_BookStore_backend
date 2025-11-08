package com.bookstore.bookstore_backend.controller;

import com.bookstore.bookstore_backend.model.ResponseMessage;
import com.bookstore.bookstore_backend.model.User.*;
import com.bookstore.bookstore_backend.repository.UserConsumptionProjection;
import com.bookstore.bookstore_backend.services.IUserService;
import com.bookstore.bookstore_backend.services.Impl.SessionTimerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private IUserService userService;

    @Autowired
    private SessionTimerService sessionTimerService;  // 新增注入

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/register")
    public ResponseMessage registerUser(@Validated @RequestBody UserDTO userDTO) {
        User user = userService.registerUser(userDTO);
        return ResponseMessage.success(user);
    }

    @PostMapping("/{userId}/address")
    public ResponseMessage addCommonAddress(@PathVariable Long userId, @Validated @RequestBody CommonAddress address) {
        User user = userService.addCommonAddress(userId, address);
        return ResponseMessage.success(user);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseMessage<?>> login(@Validated @RequestBody LoginDTO loginRequest, HttpServletRequest request) {
        logger.info("Login request for username: {}", loginRequest.getUsername());
        try {
            LoginResponseDTO response = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            // 手动触发 Spring Security 认证
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将认证信息存储到会话
            request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

            // 新增：开始计时
            sessionTimerService.startTimer();

            return ResponseEntity.ok(ResponseMessage.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseMessage.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseMessage logout(HttpServletRequest request) {
        System.out.println("Attempting to stop timer and logout.");
        long sessionDuration=1;
        try {
            sessionDuration = sessionTimerService.stopTimer();
            System.out.println("Session duration: " + sessionDuration + " seconds");
        } catch (IllegalStateException e) {
            logger.warn("Stop timer without active timer: {}", e.getMessage());
            sessionDuration = 0;
        }
        request.getSession().invalidate(); // 清除会话
        SecurityContextHolder.clearContext(); // 清除 Spring Security 上下文
        return ResponseMessage.success("登出成功，会话持续时间: " + sessionDuration + " 秒");
    }

    @GetMapping("/username/{username}")
    public ResponseMessage<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseMessage.success(user);
    }

    @GetMapping("/username/{username}/exists")
    public ResponseMessage<Boolean> checkUsernameExists(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        boolean exists = (user != null);
        return ResponseMessage.success(exists);
    }

    @GetMapping("/id/{id}")
    public ResponseMessage<User> getUserById(@PathVariable Long id) {
        logger.info("Fetching user by id: {}", id);
        User user = userService.getUserById(id);
        return ResponseMessage.success(user);
    }

    @GetMapping("/get/{userId}/addresses")
    public ResponseMessage<List<CommonAddress>> getCommonAddresses(@PathVariable Long userId) {
        List<CommonAddress> addresses = userService.getCommonAddresses( userId);
        return ResponseMessage.success(addresses);
    }

    @GetMapping("/all")
    public ResponseMessage<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseMessage.success(users);
    }

    @PutMapping("/update")
    public ResponseMessage updateUser(@Validated @RequestBody UserDTO userDTO) {
        User user = userService.updateUser(userDTO);
        return ResponseMessage.success(user);
    }

    @PutMapping("/{userId}/password")
    public ResponseMessage updatePassword(@PathVariable Long userId, @Validated @RequestBody PasswordUpdateDTO passwordDTO) {
        User user = userService.updatePassword(userId, passwordDTO.getPassword());
        return ResponseMessage.success(user);
    }

    @PutMapping("/{userId}/avatar")
    public ResponseMessage updateAvatar(@PathVariable Long userId, @Validated @RequestBody UserDTO userDTO) {
        User user = userService.updateAvatar(userId, userDTO.getAvatar());
        return ResponseMessage.success(user);
    }

    @PutMapping("/{userId}/tagline")
    public ResponseMessage updateTagLine(@PathVariable Long userId, @Validated @RequestBody UserDTO userDTO) {
        User user = userService.updateTagLine(userId, userDTO.getTagLine());
        return ResponseMessage.success(user);
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseMessage deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseMessage.success(null);
    }

    @DeleteMapping("/delete/address/{userId}/{addressId}")
    public ResponseMessage deleteAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        userService.deleteAddress(userId, addressId);
        return ResponseMessage.success(null);
    }

    @PutMapping("/disable/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage disableUser(@PathVariable Long userId) {
        User user = userService.disableUser(userId);
        return ResponseMessage.success(user);
    }

    @PutMapping("/enable/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage enableUser(@PathVariable Long userId) {
        User user = userService.enableUser(userId);
        return ResponseMessage.success(user);
    }

    @GetMapping("/consumption-ranking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseMessage<List<UserConsumptionProjection>> getUsersConsumptionRanking() {
        return ResponseMessage.success(userService.getUsersConsumptionRanking());
    }
}
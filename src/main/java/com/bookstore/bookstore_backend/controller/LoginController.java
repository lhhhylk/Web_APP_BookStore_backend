package com.bookstore.bookstore_backend.controller;

import com.bookstore.bookstore_backend.model.ResponseMessage;
import com.bookstore.bookstore_backend.model.User.LoginDTO;
import com.bookstore.bookstore_backend.model.User.LoginResponseDTO;
import com.bookstore.bookstore_backend.model.User.User;
import com.bookstore.bookstore_backend.model.User.UserDTO;
import com.bookstore.bookstore_backend.services.IUserService;
import com.bookstore.bookstore_backend.services.Impl.SessionTimerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logins")
@Scope("singleton")
public class LoginController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private IUserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private SessionTimerService sessionTimerService;  // 新增注入

    @PostMapping("/register")
    public ResponseMessage registerUser(@Validated @RequestBody UserDTO userDTO) {
        User user = userService.registerUser(userDTO);
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
        if(sessionDuration==0){
            return ResponseMessage.error("登出失败");
        }
        return ResponseMessage.success("登出成功，会话持续时间: " + sessionDuration + " 秒");

    }
}

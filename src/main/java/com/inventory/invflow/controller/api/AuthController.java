package com.inventory.invflow.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.invflow.dto.user.AdminResetPassword;
import com.inventory.invflow.dto.user.UserLoginRequest;
import com.inventory.invflow.dto.user.UserPasswordUpdateRequest;
import com.inventory.invflow.dto.user.UserRegisterRequest;
import com.inventory.invflow.dto.user.UserResponse;
import com.inventory.invflow.service.UserService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register (@RequestBody @Valid UserRegisterRequest userRegisterRequestDTO){

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(userRegisterRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login (@RequestBody @Valid UserLoginRequest userLoginRequestDTO) {
        
        return ResponseEntity.status(HttpStatus.OK).body(userService.login(userLoginRequestDTO));
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword (
        @RequestBody @Valid UserPasswordUpdateRequest userUpdateRequest) {
        
        userService.updatePassword(userUpdateRequest);

        return ResponseEntity.status(HttpStatus.OK).body("修改密碼成功");
    }

    @PutMapping("/admin/reset-password/{userName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resetPassword(
        @PathVariable String userName,
        @RequestBody @Valid AdminResetPassword request) {

            return ResponseEntity.status(HttpStatus.OK).body(
                    "已重置使用者 " + userName + " 的密碼");
        }
}

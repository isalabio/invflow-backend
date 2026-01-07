package com.inventory.invflow.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inventory.invflow.dto.user.AdminResetPassword;
import com.inventory.invflow.dto.user.UserLoginRequest;
import com.inventory.invflow.dto.user.UserRegisterRequest;
import com.inventory.invflow.dto.user.UserResponse;
import com.inventory.invflow.enums.UserRole;
import com.inventory.invflow.dto.user.UserPasswordUpdateRequest;

public interface UserService {

    // 創建
    UserResponse register (UserRegisterRequest userRegisterRequest);

    // 登入
    UserResponse login (UserLoginRequest userLoginRequest);
    
    // 更新
    UserResponse updatePassword (UserPasswordUpdateRequest userUpdateRequest);
    UserResponse updateUserRole(String username, UserRole newRole);
    Void resetPassword(String userName, AdminResetPassword adminResetPassword);

    // 查詢
    UserResponse getUserByUserName(String userName);
    Page<UserResponse> getAllUsers(Pageable pageable);
    Page<UserResponse> searchUsers(String keyword, Pageable pageable);
    Page<UserResponse> listUsers(Boolean enabled, Pageable pageable);

    // 啟用 / 停用 
    UserResponse disable (String userName);
    UserResponse enable (String userName);

}
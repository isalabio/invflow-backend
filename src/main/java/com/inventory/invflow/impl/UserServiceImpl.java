package com.inventory.invflow.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.invflow.dto.user.AdminResetPassword;
import com.inventory.invflow.dto.user.UserLoginRequest;
import com.inventory.invflow.dto.user.UserRegisterRequest;
import com.inventory.invflow.dto.user.UserResponse;
import com.inventory.invflow.dto.user.UserPasswordUpdateRequest;
import com.inventory.invflow.entity.User;
import com.inventory.invflow.enums.UserRole;
import com.inventory.invflow.exception.AccountDisabledException;
import com.inventory.invflow.exception.BadCredentialsException;
import com.inventory.invflow.exception.ResourceNotFoundException;
import com.inventory.invflow.repository.UserRepository;
import com.inventory.invflow.service.UserService;

@Transactional
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String companyDomain = "@invflow.com";

    private final static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);


    // ========= 新增 =========
    @Override
    public UserResponse register(UserRegisterRequest userRegisterRequest) {

        if(userRepository.findByUserName(userRegisterRequest.userName()).isPresent()) {
            log.warn(userRegisterRequest.userName() + " 已經被註冊過了");
            throw new BadCredentialsException(userRegisterRequest.userName() + " 已經被註冊過了");
        }

        User newUser = toUserEntity(userRegisterRequest);
        User saved = userRepository.save(newUser);

        return UserResponse.fromEntity(saved);
        }

       private User toUserEntity(UserRegisterRequest request){
            User user = new User();
            user.setUserName(request.userName().toLowerCase().trim());

            String hashPassword = passwordEncoder.encode(request.password());
            user.setPassword(hashPassword);

            user.setFullName(request.fullName().trim());
            
            String email = request.userName().toLowerCase() + companyDomain;
            user.setEmail(email.trim());

            user.setUserRole(UserRole.VIEWER);
            user.setEnabled(true);

            return user;
       }

    @Override
    public UserResponse login (UserLoginRequest userLoginRequestDTO) {

        User user = userRepository.findByUserName(userLoginRequestDTO.userName().trim())
            .orElseThrow(() -> new BadCredentialsException("帳號或密碼錯誤"));

        if(!user.getEnabled()){
            throw new AccountDisabledException("此帳號已被停用");
        }

        if(!passwordEncoder.matches(userLoginRequestDTO.password().trim(), user.getPassword().trim())){
            log.warn(userLoginRequestDTO.userName().trim() + " 密碼輸入錯誤");
            throw new BadCredentialsException("帳號或密碼錯誤");
        }

        return UserResponse.fromEntity(user);
    }


    // ========= 更新 =========
    @Override
    public UserResponse updatePassword(UserPasswordUpdateRequest userUpdateRequestDTO) {

        User user = userRepository.findByUserName(userUpdateRequestDTO.userName())
                                    .orElseThrow( () -> {log.warn(userUpdateRequestDTO.userName() + " 尚未註冊");
                                                        return new ResourceNotFoundException(userUpdateRequestDTO.userName() + " 尚未註冊");});

        if(!passwordEncoder.matches(userUpdateRequestDTO.oldPassword(), user.getPassword())){
            log.warn(userUpdateRequestDTO.userName() + "舊密碼輸入錯誤");
            throw new BadCredentialsException("舊密碼輸入錯誤");
        }

        user.setPassword(passwordEncoder.encode(userUpdateRequestDTO.newPassword()));
        userRepository.save(user);

        return UserResponse.fromEntity(user);
    }


    // ========= 查詢 =========
    @Override
    public UserResponse getUserByUserName(String userName) {

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("查無此用戶"));

        return UserResponse.fromEntity(user);
    }


    // ========= 權限 =========
    @Override
    public Void resetPassword(String userName, AdminResetPassword adminResetPassword) {

        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new UsernameNotFoundException("查無此 user"));

        String hashPassword = passwordEncoder.encode(adminResetPassword.newPassword());

        user.setPassword(hashPassword);
        userRepository.save(user);
        
        return null;
    }

    @Override
    public UserResponse disable(String userName) {
        User user = userRepository.findByUserNameAndEnabledTrue(userName)
                                    .orElseThrow(() -> new BadCredentialsException("此帳號已停用"));
        
        user.setEnabled(false);
        userRepository.save(user);

        return UserResponse.fromEntity(user);
    }

    @Override
    public UserResponse enable(String userName) {
        User user = userRepository.findByUserName(userName)
                                .orElseThrow(() -> new UsernameNotFoundException("查無此帳號"));

        if(user.getEnabled()){
            throw new BadCredentialsException("此帳號已是啟用狀態");
        }

        user.setEnabled(true);
        userRepository.save(user);

        return UserResponse.fromEntity(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {

        Page<User> users = userRepository.findAll(pageable);

        return users.map(UserResponse::fromEntity);
    }

    @Override
    public UserResponse updateUserRole(String userName, UserRole newRole) {

        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new UsernameNotFoundException("查無此用戶"));
        
        user.setUserRole(newRole);
        return UserResponse.fromEntity(user);
    }

    @Override
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {

        UserRole role = null;

        try {
            role = UserRole.valueOf(keyword.toUpperCase());
        } catch (Exception e) {}

        Page<User> user;
        
        if(role != null) {
            user = userRepository.findByUserRole(role, pageable);
        } else {
            user = userRepository.findByUserNameContainingIgnoreCaseOrderByIdAsc(keyword, pageable);
        }

        if(user.isEmpty()) {
            throw new UsernameNotFoundException("查無此用戶");
        }

        return user.map(UserResponse::fromEntity);
    }

    @Override
    public Page<UserResponse> listUsers(Boolean enabled, Pageable pageable) {

        Page<User> user;

        if(enabled == null) {
            user = userRepository.findAll(pageable);
        } else if (enabled) {
            user = userRepository.findByEnabledTrue(pageable);
        } else {
            user = userRepository.findByEnabledFalse(pageable);
        }

        return user.map(UserResponse::fromEntity);
    }
}

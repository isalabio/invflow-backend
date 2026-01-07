package com.inventory.invflow.dto.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.inventory.invflow.entity.User;
import com.inventory.invflow.enums.UserRole;

public record UserResponse(

    String userName,
    String fullName,
    String email,
    UserRole role,
    Boolean enabled,

    @DateTimeFormat (iso = DateTimeFormat.ISO.DATE)
    LocalDate createdAt,
    
    LocalDateTime lastLoginAt
) 

{
    public static UserResponse fromEntity(User user){
        return new UserResponse(
            user.getUserName(), 
            user.getFullName(),
            user.getEmail(),
            user.getUserRole(),
            user.getEnabled(),
            user.getCreatedAt().toLocalDate(),
            user.getLastLoginAt()
        );
    }
}

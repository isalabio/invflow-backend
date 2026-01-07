package com.inventory.invflow.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(

    @NotBlank(message = "用戶名稱不可為空")
    String userName,

    @NotBlank(message = "密碼不可為空")
    String password
) {}

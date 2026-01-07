package com.inventory.invflow.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequest(

    @NotBlank
    String userName,

    @NotBlank(message = "舊密碼不可為空")
    String oldPassword,

    @NotBlank(message = "新密碼不可為空")
    @Size(min = 8, max = 100, message = "新密碼長度需介於 8-100 字元")
    String newPassword
) {}

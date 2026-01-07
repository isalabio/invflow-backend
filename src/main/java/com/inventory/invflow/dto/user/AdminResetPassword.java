package com.inventory.invflow.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminResetPassword(
    @NotBlank
    @Size(min = 8, max = 12, message = "新密碼長度需介於 8-12 字元")
    String newPassword
) {}

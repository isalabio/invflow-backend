package com.inventory.invflow.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

    @NotBlank(message = "用戶名稱不可為空")
    @Size(max = 100, message = "用戶名稱不可大於 100 字元")
    String userName,

    @NotBlank(message = "密碼不可為空")
    @Size(min = 8, max = 12, message = "密碼長度需介於 8-12 字元")
    String password,

    @NotBlank(message = "姓名不可為空")
    @Size(max = 100, message = "姓名不可超過 100 字元")
    String fullName
){}

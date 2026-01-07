package com.inventory.invflow.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierCreateRequest(

    @NotBlank(message = "供應商名稱不可為空")
    @Size(max = 50, message = "供應商名稱不可超過 100 字元")
    String supplierName,

    @Size(max = 50, message = "聯絡人名稱不可超過 100 字元")
    String contactName,

    @Size(max = 100, message = "電話不可超過 100 字元")
    String phone,

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式錯誤")
    @Size(max = 255, message = "Email不可超過 255 字元")
    String email,

    @NotBlank(message = "付款條件不可為空")
    String paymentTerm,

    @Size(max = 255, message = "備註不可超過 255 字元")
    String note
) {}

package com.inventory.invflow.dto.item;

import com.inventory.invflow.enums.WineType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemCreateRequest(

    @NotBlank(message = "品項名稱不可為空")
    @Size(max = 50, message = "品項名稱不可超過 300 字元")
    String name,

    @NotNull(message = "酒類型不可為空")
    WineType wineType,

    @NotBlank(message = "產地不可為空")
    @Size(max = 10, message = "產地代碼名稱不可超過 10 字元")
    String originCountry,

    @Min(value= 1900, message = "年份不可小於1900")
    @Max(value = 2100, message = "年份不可大於 2100")
    Integer vintage,

    @NotNull(message = "容量不可為空")
    @Min(value = 50, message = "容量不可小於 50ml")
    @Max(value = 5000, message = "容量不可大於 5000ml")
    Integer volumeMl,

    @NotNull(message = "庫存不可為空")
    @Min(value = 0, message = "庫存不可為負數")
    Integer stock,

    @NotBlank(message = "供應商 ID 不可為空")
    String supplierId
) {}

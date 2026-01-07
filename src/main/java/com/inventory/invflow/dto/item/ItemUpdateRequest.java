package com.inventory.invflow.dto.item;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ItemUpdateRequest(

    @Size(max = 50, message = "品項名稱不可超過 300 字元")
    String name,

    @Size(max = 2, message = "產地代碼名稱不可超過 2 字元")
    String originCountry,

    @Min(value = 1900, message = "年份不可小於1900") 
    @Max(value = 2100, message = "年份不可大於 2100")
    Integer vintage
) {}

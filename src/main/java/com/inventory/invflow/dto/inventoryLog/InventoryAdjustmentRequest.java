package com.inventory.invflow.dto.inventoryLog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryAdjustmentRequest(

    @NotBlank(message = "商品 ID 不可為空")
    String itemId,

    @NotNull(message = "實際庫存不可為空")
    @Min(value = 0, message = "實際庫存不可為負數")
    Integer actualStock,

    @NotBlank(message = "說明原因不可為空")
    @Size(max = 255, message = "說明欄不可超過 255 字元")
    String reason
) {}

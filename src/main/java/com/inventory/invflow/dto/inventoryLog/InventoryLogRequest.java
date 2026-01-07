package com.inventory.invflow.dto.inventoryLog;

import com.inventory.invflow.enums.MovementType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record InventoryLogRequest (

    @NotBlank(message = "商品 ID 不可為空")
    String itemId,

    @NotNull(message = "異動數量不可為空")
    @Positive(message = "異動數量必須為正整數")
    Integer changeQuantity,
    
    @NotNull(message = "異動類型不可為空")
    MovementType movementType,

    @Size(max = 255, message = "備註不可超過 255 字元")
    String note
) {}

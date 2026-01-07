package com.inventory.invflow.dto.inventoryLog;


import java.time.LocalDateTime;

// import java.time.LocalDateTime;

import com.inventory.invflow.entity.InventoryLog;
import com.inventory.invflow.enums.MovementType;


public record InventoryLogResponse (

    Integer logId,
    String itemId,
    String name,
    Integer beginning,
    Integer changeQuantity,
    Integer ending,
    MovementType movementType,
    LocalDateTime createdAt,
    String createdBy,
    String note
)
    {
        public static InventoryLogResponse fromEntity (InventoryLog log) {
            return new InventoryLogResponse(
                log.getLogId(),
                log.getItem().getItemId(),
                log.getItem().getName(),
                log.getBeginning(),
                log.getChangeQuantity(),
                log.getEnding(),
                log.getMovementType(),
                log.getCreatedAt(),
                log.getCreatedBy(),
                log.getNote()
            );
        }
    }

package com.inventory.invflow.dto.dashboard;

import java.time.LocalDateTime;

import com.inventory.invflow.enums.MovementType;

public record RecentMovementRow(
    String itemId,
    String name,
    Integer beginning,
    Integer changeQuantity,
    Integer ending,
    MovementType movementType,
    String note,
    String createdBy,
    LocalDateTime createdAt
) {}

package com.inventory.invflow.dto.inventoryLog;

public record InventorySummaryResponse(
    String itemId,
    String name,
    Integer beginning,
    MovementSummary movementSummary,
    Integer ending
) {}

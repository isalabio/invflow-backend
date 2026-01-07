package com.inventory.invflow.dto.inventoryLog;

public record MovementSummary(
    Integer purchaseQuantity,
    Integer transferInQuantity,
    Integer returnInQuantity,
    Integer manualInQuantity,
    Integer saleQuantity,
    Integer transferOutQuantity,
    Integer damageQuantity,
    Integer lostQuantity,
    Integer scrapQuantity,
    Integer manualOutQuantity
) {}

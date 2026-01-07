package com.inventory.invflow.dto.dashboard;

public record LowStockRow(
    String itemId,
    String name,
    long stock,
    long safetyStock
){}
package com.inventory.invflow.dto.dashboard;

public record DashboardKpi(
    long itemCount,
    long lowStockCount,
    long todayInCount,
    long todayOutCount
) {}

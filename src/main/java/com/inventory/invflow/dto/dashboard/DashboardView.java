package com.inventory.invflow.dto.dashboard;

import java.util.List;

public record DashboardView(
    DashboardKpi kpi,
    List<RecentMovementRow> recentMovements,
    List<LowStockRow> lowStockItems
) {}

package com.inventory.invflow.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.inventory.invflow.dto.dashboard.DashboardKpi;
import com.inventory.invflow.dto.dashboard.DashboardView;
import com.inventory.invflow.dto.dashboard.LowStockRow;
import com.inventory.invflow.dto.dashboard.RecentMovementRow;
import com.inventory.invflow.enums.MovementType;
import com.inventory.invflow.repository.InventoryLogRepository;
import com.inventory.invflow.repository.ItemRepository;

@Service
public class DashboardService {
    
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    public DashboardView getDashboard() {

        // KPI counts
        long itemCount = itemRepository.countByEnabledTrue();
        long lowStockCount = itemRepository.countLowStockItems();

        // Today counts [start, end)
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        long todayIn = 0;
        long todayOut = 0;

        for (MovementType type : MovementType.values()) {
            long count = inventoryLogRepository.countByMovementTypeAndCreatedAtBetween(
                    type, start, end);

            if (type.isIn()) {
                todayIn += count;
            } else {
                todayOut += count;
            }
        }

        DashboardKpi kpi = new DashboardKpi(itemCount, lowStockCount, todayIn, todayOut);

        // Recent movements
        List<RecentMovementRow> recent = inventoryLogRepository.findRecentMovements(PageRequest.of(0, 10));

        // Low stock list
        List<LowStockRow> lowStock = itemRepository.findLowStockItems(PageRequest.of(0, 10));

        return new DashboardView(kpi, recent, lowStock);
    }
}

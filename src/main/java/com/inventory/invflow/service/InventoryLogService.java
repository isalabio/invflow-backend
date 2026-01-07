package com.inventory.invflow.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inventory.invflow.dto.inventoryLog.InventoryAdjustmentRequest;
import com.inventory.invflow.dto.inventoryLog.InventoryLogRequest;
import com.inventory.invflow.dto.inventoryLog.InventoryLogResponse;
import com.inventory.invflow.dto.inventoryLog.InventorySummaryResponse;
import com.inventory.invflow.enums.MovementType;

public interface InventoryLogService {

    // 創建
    InventoryLogResponse createLog(InventoryLogRequest log);

    // 更新
    InventoryLogResponse adjustStock(InventoryAdjustmentRequest inventoryAdjustmentRequest);

    // 查詢
    InventorySummaryResponse getSummaryByItem(String itemId, LocalDate startDate, LocalDate endDate);
    List<InventoryLogResponse> getItemLogs(String itemId, LocalDate startDate, LocalDate endDate);
    List<InventorySummaryResponse> getSummaryAll(LocalDate startDate, LocalDate endDate);
    List<InventoryLogResponse> getLogsByType(MovementType type, LocalDate starDate, LocalDate endDate);
    List<InventoryLogResponse> getRecentLogs(int a);
    Page<InventoryLogResponse> searchLogs(String keyword, MovementType type, LocalDate startDate, LocalDate endDate, Pageable pageable);
}

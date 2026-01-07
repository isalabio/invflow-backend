package com.inventory.invflow.controller.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.invflow.dto.inventoryLog.InventoryAdjustmentRequest;
import com.inventory.invflow.dto.inventoryLog.InventoryLogRequest;
import com.inventory.invflow.dto.inventoryLog.InventoryLogResponse;
import com.inventory.invflow.dto.inventoryLog.InventorySummaryResponse;
import com.inventory.invflow.enums.MovementType;
import com.inventory.invflow.service.InventoryLogService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/inventory-log")
public class InventoryLogController {

    @Autowired
    private InventoryLogService inventoryLogService;
    

    // ========= 新增 =========
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public ResponseEntity<InventoryLogResponse> createLog(@RequestBody @Valid InventoryLogRequest inventoryLogRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryLogService.createLog(inventoryLogRequest));
    }

    // ========= 更新 =========
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public ResponseEntity<InventoryLogResponse> adjustStock(@RequestBody @Valid InventoryAdjustmentRequest inventoryAdjustmentRequest) {

        return ResponseEntity.status(HttpStatus.OK).body(inventoryLogService.adjustStock(inventoryAdjustmentRequest));
    }

    // ========= 查詢 =========
    // 全品項明細
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<Page<InventoryLogResponse>> searchLogs (
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) MovementType type,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.status(HttpStatus.OK).body(inventoryLogService.searchLogs(keyword, type, start, end, pageable));
    }

    // 單品明細
    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<List<InventoryLogResponse>> getItemLogs(
        @PathVariable String itemId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.status(HttpStatus.OK).body(inventoryLogService.getItemLogs(itemId, startDate, endDate));
   }

   // 單品彙總明細
   @GetMapping("/summary/{itemId}")
   @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
   public ResponseEntity<InventorySummaryResponse> getSummaryByItem(
        @PathVariable String itemId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.status(HttpStatus.OK).body(inventoryLogService.getSummaryByItem(itemId, startDate, endDate));
   }

   // 全商品彙總明細
   @GetMapping("/summary/all")
   @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
   public ResponseEntity<List<InventorySummaryResponse>> getSummaryAll(
           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.status(HttpStatus.OK).body(inventoryLogService.getSummaryAll(startDate, endDate));
   }

   // 依異動類型明細
   @GetMapping("/summary/type")
   @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
   public ResponseEntity<List<InventoryLogResponse>> getLogsByType(
        @RequestParam MovementType type,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
            
            return ResponseEntity.status(HttpStatus.OK).body(inventoryLogService.getLogsByType(type, startDate, endDate));
        }
}

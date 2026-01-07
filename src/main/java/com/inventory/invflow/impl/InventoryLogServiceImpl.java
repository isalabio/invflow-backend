package com.inventory.invflow.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.invflow.dto.inventoryLog.InventoryAdjustmentRequest;
import com.inventory.invflow.dto.inventoryLog.InventoryLogRequest;
import com.inventory.invflow.dto.inventoryLog.InventoryLogResponse;
import com.inventory.invflow.dto.inventoryLog.InventorySummaryResponse;
import com.inventory.invflow.dto.inventoryLog.MovementSummary;
import com.inventory.invflow.entity.InventoryLog;
import com.inventory.invflow.entity.Item;
import com.inventory.invflow.entity.User;
import com.inventory.invflow.enums.MovementType;
import com.inventory.invflow.exception.ResourceNotFoundException;
import com.inventory.invflow.repository.InventoryLogRepository;
import com.inventory.invflow.repository.ItemRepository;
import com.inventory.invflow.repository.UserRepository;
import com.inventory.invflow.service.InventoryLogService;

@Transactional
@Service
public class InventoryLogServiceImpl implements InventoryLogService {

    private final static Logger log = LoggerFactory.getLogger(InventoryLogServiceImpl.class);

    @Autowired
    InventoryLogRepository inventoryLogRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    UserRepository userRepository;


    // ========= 新增 =========
    @Override
    public InventoryLogResponse createLog(InventoryLogRequest dto) {
        
        Item item = itemRepository.findById(dto.itemId().trim()).orElse(null);

        if(item == null) { 
            log.debug("查無此品項，請重新輸入");
            throw new ResourceNotFoundException("查無此品項，請重新輸入");
        }

        if(dto.changeQuantity() == null || dto.changeQuantity() <= 0 ) {
            throw new IllegalArgumentException("數量必須為正整數");
        }

        if(dto.movementType().getDirection().isNone()) {
            log.info("此異動類型需主管授權，請走調整流程");
            throw new IllegalArgumentException("此異動類型需主管授權，請走調整流程");
        }

        Integer beginning = item.getStock();
        MovementType type = dto.movementType();

        Integer changeQuantity = dto.changeQuantity();
        Integer signedQty = type.toSignedQuantity(changeQuantity);
        Integer ending = beginning + signedQty;

        if(ending < 0) {
            log.info("庫存不能小於 0 ");
            throw new IllegalArgumentException("庫存不能小於 0 ");
        }

        InventoryLog inventoryLog = new InventoryLog();
            inventoryLog.setItem(item);
            inventoryLog.setBeginning(beginning);
            inventoryLog.setChangeQuantity(signedQty);
            inventoryLog.setEnding(ending);
            inventoryLog.setMovementType(dto.movementType());
            inventoryLog.setNote(dto.note());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            String userName = authentication.getName();

            User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new ResourceNotFoundException("查無此帳號"));

            inventoryLog.setUser(user);
            
            inventoryLogRepository.save(inventoryLog);

            item.setStock(ending);
            itemRepository.save(item);


        return InventoryLogResponse.fromEntity(inventoryLog);
    }

    // ========= 更新/調整 =========
    @Override
    public InventoryLogResponse adjustStock(InventoryAdjustmentRequest request) {

        Item item = itemRepository.findByItemId(request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("查無此商品"));

        Integer systemStock = item.getStock();
        Integer acutalStock = request.actualStock();
        Integer difference = acutalStock - systemStock;

        if (difference == 0) {
            log.debug("實際庫存與系統庫存一致，無需調整");
            throw new IllegalArgumentException("實際庫存與系統庫存一致，無需調整");
        }

        InventoryLog inventoryLog = new InventoryLog();
        inventoryLog.setItem(item);
        inventoryLog.setBeginning(systemStock);
        inventoryLog.setChangeQuantity(difference);
        inventoryLog.setEnding(acutalStock);
        inventoryLog.setMovementType(MovementType.MANUAL_ADJUST);
        inventoryLog.setNote("盤點調整原因: " + request.reason() +
                "( 調整前數量: " + systemStock + ", 實際盤點數: " + acutalStock + " )");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new ResourceNotFoundException("查無此用戶"));

        inventoryLog.setUser(user);

        inventoryLogRepository.save(inventoryLog);

        item.setStock(acutalStock);
        itemRepository.save(item);

        log.info("庫存盤點調整 - itemId: {}, 差異: {}", request.itemId(), difference);
        return InventoryLogResponse.fromEntity(inventoryLog);
    }


    // ========= 查詢 =========
    @Override
    public List<InventoryLogResponse> getItemLogs(
        String itemId, LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<InventoryLog> itemLog = inventoryLogRepository.findByItem_ItemIdAndCreatedAtBetweenOrderByCreatedAtAsc(itemId, start, end);

        if(itemLog.isEmpty()){
            throw new ResourceNotFoundException("查無 item: " + itemId + " 異動明細");
        }

        return itemLog.stream()
                      .map(InventoryLogResponse::fromEntity)
                      .toList();

    }
    
    @Override
    public InventorySummaryResponse getSummaryByItem(String itemId, LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<InventoryLog> itemLogs = inventoryLogRepository.findByItem_ItemIdAndCreatedAtBetweenOrderByCreatedAtAsc(itemId, start, end);

        if(itemLogs.isEmpty()){
            throw new ResourceNotFoundException ("查無 item: " + itemId + " 資料");
        }

        InventoryLog firstLog = itemLogs.get(0);
        InventoryLog lastLog = itemLogs.get(itemLogs.size() - 1);

        Integer beginning = firstLog.getBeginning();
        Integer ending = lastLog.getEnding();

        Integer purchaseQuantity = 0;
        Integer transferInQuantity = 0;
        Integer returnInQuantity = 0;
        Integer damageQuantity = 0;
        Integer lostQuantity = 0;
        Integer scrapQuantity = 0;
        Integer transferOutQuantity = 0;
        Integer saleQuantity = 0;
        Integer manualInQuantity = 0;
        Integer manualOutQuantity = 0;

        for(InventoryLog itemLog : itemLogs){
            Integer qty = itemLog.getChangeQuantity();
            
            switch(itemLog.getMovementType()) {
                case PURCHASE:
                    purchaseQuantity += qty; break;
                case TRANSFER_IN:
                    transferInQuantity += qty; break;
                case RETURN_IN:
                    returnInQuantity += qty; break;
                case DAMAGE:
                    damageQuantity += (-qty); break;
                case LOST:
                    lostQuantity += (-qty); break;
                case SCRAP:
                    scrapQuantity += (-qty); break;
                case TRANSFER_OUT:
                    transferOutQuantity += (-qty); break;
                case SALE:
                    saleQuantity += (-qty); break;
                case MANUAL_ADJUST:
                    if(qty > 0) { manualInQuantity += qty;}
                    else { manualOutQuantity += (-qty);} break;
                default:
                    break;
            }
        }

            MovementSummary movementSummary = new MovementSummary(
                    purchaseQuantity,
                    transferInQuantity,
                    returnInQuantity,
                    manualInQuantity,
                    saleQuantity,
                    transferOutQuantity,
                    damageQuantity,
                    lostQuantity,
                    scrapQuantity,
                    manualOutQuantity
            );

            return new InventorySummaryResponse(
                    itemId,
                    firstLog.getItem().getName(),
                    beginning,
                    movementSummary,
                    ending                    
                    );
    }


    @Override
    public List<InventorySummaryResponse> getSummaryAll(LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<InventoryLog> logs = inventoryLogRepository.findByCreatedAtBetweenOrderByItem_ItemId(start, end);

        if(logs.isEmpty()){
            log.warn("查無 {} 到 {} 的異動記錄", startDate, endDate);
        }

        Map<String, List<InventoryLog>> groupByItem = 
            logs.stream().collect(Collectors.groupingBy(
                log -> log.getItem().getItemId(),TreeMap::new, Collectors.toList()));

        return groupByItem.entrySet().stream()
                           .map(entry -> calculateSummary(entry.getKey(), entry.getValue()))
                           .toList() ;
    }

    private InventorySummaryResponse calculateSummary(String itemId, List<InventoryLog> logs) {

        InventoryLog firstLog = logs.get(0);
        InventoryLog lastLog = logs.get(logs.size()-1);

        MovementSummary summary = new MovementSummary(
            sumByType(logs, MovementType.PURCHASE),
            sumByType(logs, MovementType.TRANSFER_IN),
            sumByType(logs, MovementType.RETURN_IN),
            sumByType(logs, MovementType.MANUAL_ADJUST, true),
            sumByType(logs, MovementType.SALE),
            sumByType(logs, MovementType.TRANSFER_OUT),
            sumByType(logs, MovementType.DAMAGE),
            sumByType(logs, MovementType.LOST),
            sumByType(logs, MovementType.SCRAP),
            sumByType(logs, MovementType.MANUAL_ADJUST, false)
        );

        return new InventorySummaryResponse(
            itemId,
            firstLog.getItem().getName(),
            firstLog.getBeginning(),
            summary,
            lastLog.getEnding()            
        );
    }
        
        private Integer sumByType(List<InventoryLog> logs, MovementType type) {
            
            return logs.stream()
                    .filter(log -> log.getMovementType() == type)
                    .mapToInt(log -> Math.abs(log.getChangeQuantity()))
                    .sum();
        }

        private Integer sumByType(List<InventoryLog> logs, MovementType type, boolean positive) {

            return logs.stream()
                    .filter(log -> log.getMovementType() == type)
                    .filter(log -> positive ? log.getChangeQuantity() > 0 : log.getChangeQuantity() < 0)
                    .mapToInt(log -> Math.abs(log.getChangeQuantity()))
                    .sum();
        }

    @Override
    public List<InventoryLogResponse> getLogsByType(MovementType type, LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<InventoryLog> logs = inventoryLogRepository.findByMovementTypeAndCreatedAtBetween(type, start, end);

        if (logs.isEmpty()) {
            log.warn("查無 {} 到 {} 的異動記錄", startDate, endDate);
            throw new ResourceNotFoundException("查無異動明細");
        }

        return logs.stream()
                .map(InventoryLogResponse::fromEntity)
                .toList();
    }

    @Override
    public List<InventoryLogResponse> getRecentLogs(int a) {

        List<InventoryLog> logList = inventoryLogRepository.findByOrderByLogIdDesc();

        return logList.stream()
                      .limit(a)
                      .map(InventoryLogResponse::fromEntity)
                      .toList();
                      
    }

    @Override
    public Page<InventoryLogResponse> searchLogs(
        String keyword, MovementType type, LocalDate startDate, LocalDate endDate, Pageable pageable) {
            
            LocalDateTime startAt = startDate.atStartOfDay();
            LocalDateTime endAt = endDate.plusDays(1).atStartOfDay().minusNanos(1);

            Page<InventoryLog> page = inventoryLogRepository.searchLogs(keyword, type, startAt, endAt, pageable);

            return page.map(InventoryLogResponse::fromEntity);
        }
}

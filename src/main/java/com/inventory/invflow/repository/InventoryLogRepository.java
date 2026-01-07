package com.inventory.invflow.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.inventory.invflow.dto.dashboard.RecentMovementRow;
import com.inventory.invflow.entity.InventoryLog;
import com.inventory.invflow.enums.MovementType;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Integer> {
    
    Optional<InventoryLog> findByItem_ItemId (String itemId);
    Optional<InventoryLog> findTopByItem_ItemIdOrderByCreatedAtDesc(String itemId);

    List<InventoryLog> findByItem_ItemIdAndCreatedAtBetweenOrderByCreatedAtAsc(String itemId, LocalDateTime start, LocalDateTime end);
    List<InventoryLog> findByCreatedAtBetweenOrderByItem_ItemId(LocalDateTime start, LocalDateTime end);
    List<InventoryLog> findByMovementTypeAndCreatedAtBetween(MovementType type, LocalDateTime start, LocalDateTime end);
    List<InventoryLog> findByOrderByLogIdDesc();

    @Query("SELECT l " +
            "FROM InventoryLog l " +
            "JOIN l.item i " +
            "LEFT JOIN i.supplier s " +
            "WHERE l.createdAt >= :startAt " +
            "AND l.createdAt < :endAt " +
            "AND (:type IS NULL OR l.movementType = :type) " +
            "AND ( " +
            "  :keyword IS NULL OR :keyword = '' OR " +
            "  i.itemId LIKE CONCAT('%', :keyword, '%') OR " +
            "  i.name   LIKE CONCAT('%', :keyword, '%') OR " +
            "  s.supplierName   LIKE CONCAT('%', :keyword, '%') " +
            ") ")
    Page<InventoryLog> searchLogs(
        @Param("keyword") String keyword,
        @Param("type") MovementType type,
        @Param("startAt") LocalDateTime startAt,
        @Param("endAt") LocalDateTime endAt,
        Pageable pageable
    );

    // Dashboard 今日出/入筆數＆最近異動
    @Query("""
            SELECT count(l)
            FROM InventoryLog l
            WHERE l.movementType = :type
            and l.createdAt >= :start
            and l.createdAt < :end
        """)
    long countByMovementTypeAndCreatedAtBetween(
        @Param("type") MovementType type,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT new com.inventory.invflow.dto.dashboard.RecentMovementRow(
                i.itemId, i.name, l.beginning, l.changeQuantity, l.ending, l.movementType, l.note, l.createdBy, l.createdAt
            )
            FROM InventoryLog l
            JOIN l.item i
            ORDER BY l.createdAt desc
        """)
    List<RecentMovementRow> findRecentMovements(Pageable pageable);
}

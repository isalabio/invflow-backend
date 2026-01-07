package com.inventory.invflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.inventory.invflow.dto.dashboard.LowStockRow;
import com.inventory.invflow.entity.Item;
import com.inventory.invflow.enums.WineType;

import java.util.List;
import java.util.Optional;


public interface ItemRepository extends JpaRepository<Item, String>{

    Optional<Item> findTopByWineTypeOrderBySkuDesc(WineType wineType);
    Optional<Item> findTopByOrderByItemIdDesc();
    Optional<Item> findTopBySkuStartingWithOrderBySkuDesc(String prefix);
    Optional<Item> findByNameAndVolumeMlAndWineType(String name, Integer volumeMl, WineType wineType);
    Optional<Item> findByItemId(String itemId);
    Optional<Item> findBySku(String sku);
    Optional<Item> findByItemIdAndSku(String itemId, String sku);

    Optional<Item> findByItemIdAndEnabledTrue(String itemId);
    Optional<Item> findBySkuAndEnabledTrue(String sku);
    List<Item> findByEnabledTrueOrderByItemIdDesc();
    Page<Item> findByEnabledFalse(Pageable pageable);
    Page<Item> findByEnabledTrue(Pageable pageable);

    List<Item> findAllByOrderByCreatedAtDesc();
    List<Item> findByEnabledTrueAndStockLessThanOrderByItemIdAsc(Integer stockThreshold);
    Page<Item> findByItemIdContainingIgnoreCaseOrNameContainingIgnoreCaseOrSkuOrSupplier_SupplierIdOrderByItemId(String itemIdKeyword, String nameKeyword, String sku, String supplierId, Pageable pageable);

    List<Item> findBySupplier_SupplierId(String supplierId);
    List<Item> findBySupplier_SupplierIdAndEnabledTrueOrderByCreatedAtDesc(String supplierId);

    // Dashboard 統計/清單 查詢
    long countByEnabledTrue();

    @Query("""
            SELECT count(i)
            FROM Item i
            WHERE i.enabled = true
              AND i.stock < i.safetyStock
            """)
    long countLowStockItems();

    @Query("""
            SELECT new com.inventory.invflow.dto.dashboard.LowStockRow(
                i.itemId, i.name, i.stock, i.safetyStock
            )
            FROM Item i
            WHERE i.enabled = true
              AND i.stock < i.safetyStock
            ORDER BY (i.safetyStock - i.stock) desc
            """)

    List<LowStockRow> findLowStockItems(Pageable pageable);
}
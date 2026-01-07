package com.inventory.invflow.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inventory.invflow.dto.item.ItemCreateRequest;
import com.inventory.invflow.dto.item.ItemResponse;
import com.inventory.invflow.dto.item.ItemUpdateRequest;
import com.inventory.invflow.enums.WineType;


public interface ItemService {

    // 創建
    ItemResponse createItem(ItemCreateRequest item);
    String generateSku(WineType wineType, Integer volumeMl);
    String generateItemId();

    // 更新
    ItemResponse updatePartial(String itemId, ItemUpdateRequest item);

    // 查詢
    ItemResponse getItemById(String itemId);
    ItemResponse getItemBySku(String sku);
    List<ItemResponse> getItemBySupplierId(String supplierId);
    Page<ItemResponse> getAllItem(Pageable pageable);
    List<ItemResponse> getEnabledItem();
    List<ItemResponse> getLowStockItems(Integer threshold);
    Page<ItemResponse> searchItems(String keyword, Pageable pageable);
    Page<ItemResponse> listItems(Boolean enabled, Pageable pageable);

    // 啟用 / 停用
    ItemResponse enableItem(String itemId);
    ItemResponse disableItem(String itemId);

   

}

    

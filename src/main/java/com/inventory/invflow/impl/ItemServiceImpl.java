package com.inventory.invflow.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.inventory.invflow.dto.item.ItemCreateRequest;
import com.inventory.invflow.dto.item.ItemResponse;
import com.inventory.invflow.dto.item.ItemUpdateRequest;
import com.inventory.invflow.entity.Item;
import com.inventory.invflow.entity.Supplier;
import com.inventory.invflow.enums.WineType;
import com.inventory.invflow.exception.DuplicateResourceException;
import com.inventory.invflow.exception.ResourceNotFoundException;
import com.inventory.invflow.repository.ItemRepository;
import com.inventory.invflow.repository.SupplierRepository;
import com.inventory.invflow.service.ItemService;

@Transactional
@Service
public class ItemServiceImpl implements ItemService{

    private final static Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private SupplierRepository supplierRepository;
    

    // ========= 新增 =========
    @Override
    public ItemResponse createItem(ItemCreateRequest itemRequest) {

        if (itemRepository
                .findByNameAndVolumeMlAndWineType(itemRequest.name().trim(), itemRequest.volumeMl(), itemRequest.wineType())
                .isPresent()) {

            log.warn("嘗試建立重複品項 - name: {}, volumeMl: {}, wineType: {}", 
                itemRequest.name(), 
                itemRequest.volumeMl(), 
                itemRequest.wineType());

            throw new DuplicateResourceException(itemRequest.name().trim() + " 同規格商品已存在，不可重複建立");
        }

        Item newItem = toItemEntity(itemRequest);
        Item savedItem = itemRepository.save(newItem);

        log.info("成功建立商品 - itemId: {}, sku: {}", savedItem.getItemId(), savedItem.getSku());
        return ItemResponse.fromEntity(savedItem);
    }

    private Item toItemEntity(ItemCreateRequest request) {
        Item item = new Item();
        item.setItemId(generateItemId());
        item.setSku(generateSku(request.wineType(), request.volumeMl()));
        item.setName(request.name().trim());
        item.setWineType(request.wineType());
        item.setOriginCountry(request.originCountry().trim().toUpperCase());
        item.setVintage(request.vintage());
        item.setVolumeMl(request.volumeMl());
        item.setStock(request.stock());
        item.setEnabled(true);

        Supplier supplier = supplierRepository.findBySupplierId(request.supplierId().trim())
                .orElseThrow(() -> new ResourceNotFoundException("查無此供應商"));

        item.setSupplier(supplier);

        return item;
    }

    @Override
    public String generateSku(WineType wineType, Integer volumeMl) {

        String typeCode = wineType.getCode();
        String volumeCode = String.format("%04d", volumeMl);
        String prefix = typeCode + volumeCode;

        Optional<Item> newSku = itemRepository.findTopBySkuStartingWithOrderBySkuDesc(prefix);

        if (newSku.isEmpty()) {
            return prefix + "0001";
        }

        String lastsku = newSku.get().getSku();
        int num = Integer.parseInt(lastsku.substring(prefix.length()));
        int next = num + 1;

        return prefix + String.format("%04d", next);
    }

    @Override
    public String generateItemId() {

        Optional<Item> lastItemId = itemRepository.findTopByOrderByItemIdDesc();
        String prefix = "IT";

        if (lastItemId.isEmpty()) {
            return prefix + "000001";
        }

        String lastId = lastItemId.get().getItemId();
        int num = Integer.parseInt(lastId.substring(prefix.length()));
        int next = num + 1;

        return prefix + String.format("%06d", next);
    }


    // ========= 更新 =========
    @Override
    public ItemResponse updatePartial(String itemId, ItemUpdateRequest request) {
        
        Item existing = itemRepository.findById(itemId.trim())
            .orElseThrow (() -> 
                new ResourceNotFoundException("⚠️ 查無此 item Id: " + itemId));
        
        applyPatch(existing, request);       
        itemRepository.save(existing);
            
        log.info("成功更新商品 - itemId: {}", itemId);
        return ItemResponse.fromEntity(existing);
    }

    private void applyPatch(Item existing, ItemUpdateRequest updateData){        
        if(StringUtils.hasText(updateData.name()))
            existing.setName(updateData.name().trim());

        if(StringUtils.hasText(updateData.originCountry()))
            existing.setOriginCountry(updateData.originCountry().trim());
        
        if(updateData.vintage() != null)
            existing.setVintage(updateData.vintage());
    }


    // ========= 查詢 =========
    @Override
    public ItemResponse getItemById(String itemId) {
        Item item = itemRepository.findByItemId(itemId.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "查無符合 itemId: " + itemId + " 的品項"));

        log.debug("查詢商品 by ID - itemId: {}", itemId);
        return ItemResponse.fromEntity(item);
    }
       
    @Override
    public ItemResponse getItemBySku(String sku) {
        Item item = itemRepository.findBySku(sku.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "查無符合 sku: " + sku + " 的品項"));

        log.debug("查詢商品 by SKU - sku: {}", sku);
        return ItemResponse.fromEntity(item);
    }

    @Override
    public List<ItemResponse> getItemBySupplierId(String supplierId) {
        supplierRepository.findById(supplierId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("查無此供應商"));

        List<Item> items = itemRepository.findBySupplier_SupplierIdAndEnabledTrueOrderByCreatedAtDesc(supplierId);

        if (items.isEmpty()) {
            log.info("供應商 {} 目前沒有啟用的商品", supplierId);
            throw new ResourceNotFoundException("⚠️ 此供應商目前沒有啟用的商品");
        }

        return items.stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }

    @Override
    public Page<ItemResponse> getAllItem(Pageable pageable) {

        Page<Item> item = itemRepository.findAll(pageable);

        if (item.isEmpty()) {
            log.info("查無 {} ", item);
        }

        return item.map(ItemResponse::fromEntity);
    }

    @Override
    public List<ItemResponse> getEnabledItem() {
        List<Item> itemList = itemRepository.findByEnabledTrueOrderByItemIdDesc();

        if (itemList.isEmpty()) {
            log.warn("目前沒有啟用的商品");
            throw new ResourceNotFoundException("沒有可銷售品項");
        }

        return itemList.stream()
                .map(ItemResponse::fromEntity)
                .toList();
    }


    // ========= 權限：啟用 / 停用 =========
    @Override
    public ItemResponse enableItem(String itemId) {

        Item item = itemRepository.findByItemId(itemId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("⚠️ 查無此商品"));

        if (item.getEnabled()) {
            throw new RuntimeException("此商品已是啟用狀態");
        }

        item.setEnabled(true);
        itemRepository.save(item);

        log.info("啟用商品 - itemId: {}", itemId);
        return ItemResponse.fromEntity(item);
    }

    @Override
    public ItemResponse disableItem(String itemId) {

       Item item = itemRepository.findByItemIdAndEnabledTrue(itemId.trim())
                      .orElseThrow(() -> new ResourceNotFoundException("⚠️ 查無此啟用中的商品"));

        item.setEnabled(false);
        itemRepository.save(item);

        log.info("停用商品 - itemId: {}", itemId);
        return ItemResponse.fromEntity(item);
    }   

    // view controller
    @Override
    public List<ItemResponse> getLowStockItems(Integer threshold) {

        List<Item> items = itemRepository.findByEnabledTrueAndStockLessThanOrderByItemIdAsc(threshold);

        return items.stream()
                    .map(ItemResponse::fromEntity)
                    .toList();
    }

    @Override
    public Page<ItemResponse> searchItems (String keyword, Pageable pageable) {

        Page<Item> item = itemRepository.findByItemIdContainingIgnoreCaseOrNameContainingIgnoreCaseOrSkuOrSupplier_SupplierIdOrderByItemId(keyword, keyword, keyword, keyword, pageable);

        if(item.isEmpty()) {
            log.debug("查無此品項");
            // throw new ResourceNotFoundException("查無此品項");
        } 

        return item.map(ItemResponse::fromEntity);
    }

    @Override
    public Page<ItemResponse> listItems(Boolean enabled, Pageable pageable) {

       Page<Item> items;
       
       if(enabled == null) {
            items = itemRepository.findAll(pageable);
       } else if (enabled) {
            items = itemRepository.findByEnabledTrue(pageable);
       } else {
            items = itemRepository.findByEnabledFalse(pageable);
       }

       return items.map(ItemResponse::fromEntity);
    } 
}

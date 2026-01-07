package com.inventory.invflow.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.invflow.dto.item.ItemCreateRequest;
import com.inventory.invflow.dto.item.ItemResponse;
import com.inventory.invflow.dto.item.ItemUpdateRequest;
import com.inventory.invflow.service.ItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;
    
    
    // ========= 新增 =========
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public ResponseEntity<ItemResponse> create(@RequestBody @Valid ItemCreateRequest item){

        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(item));
    }

    // ========= 更新 =========
    @PutMapping("/update/{itemId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable String itemId, 
            @RequestBody @Valid ItemUpdateRequest item){

        return ResponseEntity.status(HttpStatus.OK).body(itemService.updatePartial(itemId, item));
    }

    // ========= 查詢 =========
    @GetMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable String itemId) {

        return ResponseEntity.status(HttpStatus.OK).body(itemService.getItemById(itemId));
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<ItemResponse> getItemBySku(@PathVariable String sku) {
        
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getItemBySku(sku));
    }

    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<List<ItemResponse>> getItemBySuppleirId(@PathVariable String supplierId) {

        return ResponseEntity.status(HttpStatus.OK).body(itemService.getItemBySupplierId(supplierId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<Page<ItemResponse>> getAllItem(Pageable pageable) {
        
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getAllItem(pageable));
    }

    @GetMapping("/get-enabled")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<List<ItemResponse>> getEnabledItem() {

        return ResponseEntity.status(HttpStatus.OK).body(itemService.getEnabledItem());
    }


    // ========= 權限：啟用 / 停用 =========
    @PutMapping("/{itemId}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ItemResponse> enable(@PathVariable String itemId) {

        return ResponseEntity.status(HttpStatus.OK).body(itemService.enableItem(itemId));
    }

    @PutMapping("/{itemId}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ItemResponse> disable(@PathVariable String itemId) {

        return ResponseEntity.status(HttpStatus.OK).body(itemService.disableItem(itemId));
    } 
}

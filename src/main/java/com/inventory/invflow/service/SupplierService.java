package com.inventory.invflow.service;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inventory.invflow.dto.supplier.SupplierCreateRequest;
import com.inventory.invflow.dto.supplier.SupplierUpdateRequest;
import com.inventory.invflow.dto.supplier.SupplierResponse;

public interface SupplierService {

    // 創建
    SupplierResponse createSupplier(SupplierCreateRequest supplierCreateRequest);
    String generateSupplierId();

    // 更新
    SupplierResponse updatePartial (String supplierId, SupplierUpdateRequest supplierUpdateRequest);

    // 查詢
    SupplierResponse getSupplierById(String supplierId);
    Page<SupplierResponse> getAllSuppliers(Pageable pageable);
    Page<SupplierResponse> getEnabledSuppliers(Pageable pageable);
    List<SupplierResponse> getEnabledSuppliers();
    
    Page<SupplierResponse> searchSuppliers(String keyword, Boolean enabled, Pageable pageable);
    Page<SupplierResponse> listSuppliers(Boolean enabled, Pageable pageable);

    // 刪除備註
    SupplierResponse deleteNote(String supplierId);

    // 啟用 / 停用
    SupplierResponse enableSupplier(String supplierId);
    SupplierResponse disableSupplier(String supplierId);
}

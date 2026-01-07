package com.inventory.invflow.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.invflow.dto.supplier.SupplierCreateRequest;
import com.inventory.invflow.dto.supplier.SupplierResponse;
import com.inventory.invflow.dto.supplier.SupplierUpdateRequest;
import com.inventory.invflow.service.SupplierService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;
    
    
    // ========= 新增 =========
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public ResponseEntity<SupplierResponse> createSupplier(@RequestBody @Valid SupplierCreateRequest supplierCreateRequest){

        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(supplierCreateRequest));
    }

    // ========= 更新 =========
    @PutMapping("/update/{supplierId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable String supplierId, 
            @RequestBody @Valid SupplierUpdateRequest request ){

       return ResponseEntity.status(HttpStatus.OK).body(supplierService.updatePartial(supplierId, request));
    }

    // ========= 查詢 =========
    @GetMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable String supplierId){
        
        return ResponseEntity.status(HttpStatus.OK).body(supplierService.getSupplierById(supplierId));
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(supplierService.getAllSuppliers(pageable));
    }

    @GetMapping("/get-enabled")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER')")
    public ResponseEntity<List<SupplierResponse>> getEnabledSupplier() {

        return ResponseEntity.status(HttpStatus.OK).body(supplierService.getEnabledSuppliers());
    }

    // ========= 刪除備註 =========
    @DeleteMapping("/{supplierId}/note")
    @PreAuthorize("hasAnyRole('MANAGER', 'OPERATOR')")
    public ResponseEntity<SupplierResponse> deleteNote(@PathVariable String supplierId) {
       
       return ResponseEntity.status(HttpStatus.OK).body(supplierService.deleteNote(supplierId));
    }

    // ========= 權限：更新供應商狀態 =========
    @PutMapping("/enable/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierResponse> enable(@PathVariable String supplierId) {
        
        return ResponseEntity.status(HttpStatus.OK).body(supplierService.enableSupplier(supplierId));
    }
    
    @PutMapping("/disable/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<SupplierResponse> disable(@PathVariable String supplierId) {
        
        return ResponseEntity.status(HttpStatus.OK).body(supplierService.disableSupplier(supplierId));
    }
}

package com.inventory.invflow.impl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.inventory.invflow.dto.supplier.SupplierCreateRequest;
import com.inventory.invflow.dto.supplier.SupplierResponse;
import com.inventory.invflow.dto.supplier.SupplierUpdateRequest;
import com.inventory.invflow.entity.Supplier;
import com.inventory.invflow.exception.DuplicateResourceException;
import com.inventory.invflow.exception.ResourceNotFoundException;
import com.inventory.invflow.repository.SupplierRepository;
import com.inventory.invflow.service.SupplierService;


@Transactional
@Service
public class SupplierServiceImpl implements SupplierService {

    private final static Logger log = LoggerFactory.getLogger(SupplierServiceImpl.class);

    @Autowired
    private SupplierRepository supplierRepository;


    // ========= 新增供應商資料 =========
    @Override
    public SupplierResponse createSupplier(SupplierCreateRequest supplierCreateRequest) {

        if (supplierRepository.findBySupplierName(supplierCreateRequest.supplierName()).isPresent()) {
            log.warn("嘗試建立重複供應商 - name: {}", supplierCreateRequest.supplierName());
            throw new DuplicateResourceException(supplierCreateRequest.supplierName() + " 廠商已存在，不可重複建立");
        }

        Supplier newSupplier = toSupplierEntity(supplierCreateRequest);
        Supplier saved = supplierRepository.save(newSupplier);

        log.info("成功新增供應商 - supplierId {}", saved.getSupplierId());
        return SupplierResponse.fromEntity(saved);
    }

        private Supplier toSupplierEntity (SupplierCreateRequest request){
            Supplier supplier = new Supplier();
            supplier.setSupplierId(generateSupplierId().trim());
            supplier.setSupplierName(request.supplierName().trim());
            supplier.setContactName(StringUtils.hasText(request.contactName()) ? request.contactName().trim() : null);
            supplier.setPhone(StringUtils.hasText(request.phone()) ? request.phone().trim() : null);
            supplier.setEmail(request.email().trim());
            supplier.setPaymentTerm(request.paymentTerm().trim());
            supplier.setNote(request.note() != null ? request.note().trim() : null);
            supplier.setEnabled(true);
            return supplier;
        }

    @Override
    public String generateSupplierId() {

        Optional<Supplier> lastSupplierId = supplierRepository.findTopByOrderBySupplierIdDesc();
        String prefix = "SU";

        if (lastSupplierId.isEmpty()) {
            return prefix + "000001";
        }

        String lastId = lastSupplierId.get().getSupplierId();
        int num = Integer.parseInt(lastId.substring(prefix.length()));
        int next = num + 1;

        return prefix + String.format("%06d", next);
    }


    // ========= 更新供應商資料 =========
    @Override
    public SupplierResponse updatePartial(String supplierId, SupplierUpdateRequest supplierUpdateRequest) {

        Supplier existing = supplierRepository.findBySupplierId(supplierId.trim())
            .orElseThrow(() -> new ResourceNotFoundException("⚠️ 查無此廠商ID"));

        String newName = supplierUpdateRequest.supplierName();
        if(newName != null && supplierRepository.existsBySupplierNameAndSupplierIdNot(newName, existing.getSupplierId())) {
            throw new DuplicateResourceException("廠商名稱已存在，不可重複");
        }
        
        applyPatch(existing, supplierUpdateRequest);

        supplierRepository.save(existing);

        log.info("成功更新供應商 - supplierId: {}", supplierId);
        return SupplierResponse.fromEntity(existing);
    }

    private void applyPatch(Supplier existing, SupplierUpdateRequest updateData){
        
        if(StringUtils.hasText(updateData.supplierName()))
            existing.setSupplierName(updateData.supplierName().trim()); 
        
        if(StringUtils.hasText(updateData.contactName()))
            existing.setContactName(updateData.contactName().trim());

        if(StringUtils.hasText(updateData.phone()))
            existing.setPhone(updateData.phone().trim());

        if(StringUtils.hasText(updateData.email()))
            existing.setEmail(updateData.email().trim());

        if(StringUtils.hasText(updateData.paymentTerm()))
            existing.setPaymentTerm(updateData.paymentTerm().trim());

        if(StringUtils.hasText(updateData.note()))
            existing.setNote(updateData.note().trim());
    }


    // ========= 查詢供應商 =========
    @Override
    public SupplierResponse getSupplierById(String supplierId) {

        Supplier supplier = supplierRepository.findBySupplierId(supplierId.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "查無此廠商"));

        log.debug("查詢供應商 - supplierId: {}", supplierId);
        return SupplierResponse.fromEntity(supplier);
    }

    @Override
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {

        Page<Supplier> suppliers = supplierRepository.findAll(pageable);

        if(suppliers.isEmpty()) {
            log.warn("目前沒有任何供應商");
        }

        return suppliers.map(SupplierResponse::fromEntity);
    }

    @Override
    public Page<SupplierResponse> getEnabledSuppliers(Pageable pageable) {
        
        Page<Supplier> suppliers = supplierRepository.findByEnabledTrue(pageable);

        if(suppliers.isEmpty()){
            log.warn("目前沒有啟用的供應商");
            throw new ResourceNotFoundException("沒有啟用中的供應商");
        }
        
        return suppliers.map(SupplierResponse::fromEntity);
    }


    // ========= 刪除備註資料 =========
    @Override
    public SupplierResponse deleteNote(String supplierId) {
        
        Supplier supplier = supplierRepository.findBySupplierId(supplierId.trim())
            .orElseThrow(() -> new ResourceNotFoundException("查無此廠商 ID: " + supplierId));

        if(supplier.getNote() == null || supplier.getNote().isBlank()){
            throw new RuntimeException("此廠商沒有可刪除的備註");
        }

        supplier.setNote(null);
        supplierRepository.save(supplier);

        return SupplierResponse.fromEntity(supplier);
    }


    // ========= 啟用 / 停用商品 =========
    @Override
    public SupplierResponse enableSupplier(String supplierId) {

        Supplier supplier = supplierRepository.findBySupplierId(supplierId.trim())
            .orElseThrow(() -> new ResourceNotFoundException("查無此廠商"));

        if (supplier.getEnabled()) {
            throw new IllegalStateException("此廠商已是啟用狀態");
        }

        supplier.setEnabled(true);
        supplierRepository.save(supplier);

        log.info("啟用供應商 - supplierId: {}", supplierId);
        return SupplierResponse.fromEntity(supplier);
    }


    @Override
    public SupplierResponse disableSupplier(String supplierId) {

        Supplier supplier = supplierRepository.findBySupplierIdAndEnabledTrue(supplierId.trim())
            .orElseThrow(() -> new ResourceNotFoundException("⚠️ 查無此啟用中的廠商"));

        supplier.setEnabled(false);
        supplierRepository.save(supplier);

        log.info("停用供應商 - supplierId: {}", supplierId);
        return SupplierResponse.fromEntity(supplier);
    }

    // view controller 
    @Override
    public Page<SupplierResponse> listSuppliers(Boolean enabled, Pageable pageable) {

        Page<Supplier> supplier;

        if(enabled == null) {
            supplier = supplierRepository.findAll(pageable);
        } else if (enabled) {
            supplier = supplierRepository.findByEnabledTrue(pageable);
        } else {
            supplier = supplierRepository.findByEnabledFalse(pageable);
        }

        return supplier.map(SupplierResponse::fromEntity);
    }

    @Override
    public Page<SupplierResponse> searchSuppliers(String keyword, Boolean enabled, Pageable pageable) {

        if(StringUtils.hasText(keyword) && enabled != null) {
            return supplierRepository.searchByKeywordAndEnabled(keyword, enabled, pageable).map(SupplierResponse::fromEntity);
        }

        if(StringUtils.hasText(keyword)) {
            return supplierRepository.findBySupplierIdContainingIgnoreCaseOrSupplierNameContainingIgnoreCaseOrContactNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                                        keyword, keyword, keyword, keyword, pageable)
                                     .map(SupplierResponse::fromEntity);
        }

        if(enabled != null ) {
            return supplierRepository.findByEnabled(enabled, pageable).map(SupplierResponse::fromEntity);
        }

        return supplierRepository.findAll(pageable).map(SupplierResponse::fromEntity);
    }

    @Override
    public List<SupplierResponse> getEnabledSuppliers() {

        List<Supplier> list = supplierRepository.findByEnabledTrue(Sort.by("createdAt").descending());

        if(list.isEmpty()) {
            log.debug("目前無啟用中的廠商");
        }

        return list.stream()
                   .map(SupplierResponse::fromEntity)
                   .toList();
    }
}

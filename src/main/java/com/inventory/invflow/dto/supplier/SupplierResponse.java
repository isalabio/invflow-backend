package com.inventory.invflow.dto.supplier;

import com.inventory.invflow.entity.Supplier;

public record SupplierResponse(

    String supplierId,
    String supplierName,
    String contactName,
    String phone,
    String email,
    String paymentTerm,
    String note,
    Boolean enabled,
    String createdBy,
    String updatedBy
)

{
    public static SupplierResponse fromEntity(Supplier supplier) {
        return new SupplierResponse(
            supplier.getSupplierId(), 
            supplier.getSupplierName(), 
            supplier.getContactName(),
            supplier.getPhone(),
            supplier.getEmail(),
            supplier.getPaymentTerm(),
            supplier.getNote(),
            supplier.getEnabled(),
            supplier.getCreatedBy(),
            supplier.getUpdatedBy()
            );
    }
}

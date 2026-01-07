package com.inventory.invflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.inventory.invflow.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, String>{
    
    Optional<Supplier> findBySupplierName(String supplierName);
    Optional<Supplier> findTopByOrderBySupplierIdDesc();
    Optional<Supplier> findBySupplierId(String supplierId);
    Optional<Supplier> findBySupplierIdAndEnabledTrue(String supplierId);

    Page<Supplier> findByEnabledTrue(Pageable pageable);
    List<Supplier> findByEnabledTrue(Sort sort);
    Page<Supplier> findByEnabled(Boolean enabled, Pageable pageable);
    Page<Supplier> findByEnabledFalse(Pageable pageable);
    Page<Supplier> findBySupplierIdContainingIgnoreCaseOrSupplierNameContainingIgnoreCaseOrContactNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(String supplierIdKeyword, String supplierNameKeyword, String contactName, String phone, Pageable pageable);
    boolean existsBySupplierNameAndSupplierIdNot(String supplierName, String supplierId);

    @Query("""
    SELECT s FROM Supplier s
    WHERE (s.supplierId LIKE %:keyword%
        OR s.supplierName LIKE %:keyword%
        OR s.contactName LIKE %:keyword%
        OR s.phone LIKE %:keyword%)
        AND s.enabled = :enabled""")
    Page<Supplier> searchByKeywordAndEnabled(
        @Param("keyword") String keyword,
        @Param("enabled") boolean enabled,
        Pageable pageable
    );
}

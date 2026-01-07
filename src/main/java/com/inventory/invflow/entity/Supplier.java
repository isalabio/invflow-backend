package com.inventory.invflow.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Supplier {
    
    @Id
    @Column(name = "supplier_id", length = 8, nullable = false)
    String supplierId;

    @Column(name = "supplier_name", length = 100, nullable = false)
    String supplierName;

    @Column(name = "contact_name", length = 100)
    String contactName;

    @Column(name = "phone", length = 50)
    String phone;

    @Column(name = "email", length = 255, nullable = false)
    String email;

    @Column(name = "enabled", nullable = false)
    Boolean enabled;

    @Column(name = "payment_term", nullable = false)
    String paymentTerm;

    @Column(name = "note", length = 255)
    String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedTime;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    String updatedBy;
}

package com.inventory.invflow.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.inventory.invflow.enums.WineType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table( name = "item")
@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Item {

    @Id
    @Column(name = "item_id", length = 8, nullable = false)
    String itemId;

    @Column(name = "sku", length = 100, nullable = false, unique = true)
    String sku;
    
    @Column(name = "name", length = 255, nullable = false)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "wine_type", length = 20, nullable = false)
    WineType wineType;

    @Column(name = "origin_country", length = 5, nullable = false)
    String originCountry;

    @Column(name = "vintage", nullable = false)
    Integer vintage;

    @Column(name = "volume_ml")
    Integer volumeMl;

    @Column(name = "stock", nullable = false)
    Integer stock;

    @Builder.Default
    @Column(name = "safety_stock", nullable = false)
    Integer safetyStock = 10;

    @Column(name = "enabled", nullable = false)
    Boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name= "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    Supplier supplier;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    String updatedBy;
}

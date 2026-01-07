package com.inventory.invflow.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.inventory.invflow.enums.MovementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) 
public class InventoryLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", nullable = false)
    Integer logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    Item item;

    @Column(name = "beginning", nullable = false)
    Integer beginning;

    @Column(name = "ending", nullable = false)
    Integer ending;

    @Column(name = "change_quantity", nullable = false)
    Integer changeQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", length = 255)
    MovementType movementType;

    @Column( name = "note", length = 255)
    String note;

    @CreationTimestamp
    @Column( name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column( name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( name = "user_id", nullable = false)
    User user;

    @CreatedBy
    @Column( name = "created_by", updatable = false)
    String createdBy;

    @LastModifiedBy
    @Column( name = "updated_by")
    String updatedBy;
    
}

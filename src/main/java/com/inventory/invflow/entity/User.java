package com.inventory.invflow.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inventory.invflow.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "id")
    Integer id;

    @Column( name = "user_name", length = 50, nullable = false)
    String userName;

    @Column( name = "password", length = 500, nullable = false)
    @JsonIgnore
    String password;

    @Column( name = "full_name", length = 50, nullable = false)
    String fullName;

    @Column( name = "email", length = 100, nullable = false)
    String email;

    @Enumerated(EnumType.STRING)
    @Column( name = "user_role", length = 20, nullable = false)
    @JsonProperty("user_role")
    UserRole userRole;

    @Column( name = "enabled", nullable = false)
    Boolean enabled;

    @CreationTimestamp
    @Column( name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column( name = "updated_at", nullable = false)
    LocalDateTime updatedTime;

    @Column( name = "last_login_at")
    LocalDateTime lastLoginAt;
}

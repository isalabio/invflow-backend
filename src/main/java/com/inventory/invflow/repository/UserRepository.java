package com.inventory.invflow.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.inventory.invflow.entity.User;
import com.inventory.invflow.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserName(String userName);
    Optional<User> findByUserNameAndEnabledTrue(String userName);

    Page<User> findByEnabledTrue(Pageable pageable);
    Page<User> findByEnabledFalse(Pageable pageable);
    Page<User> findByUserRole(UserRole role, Pageable pageable);
    Page<User> findByUserNameContainingIgnoreCaseOrderByIdAsc(String userName, Pageable pageable);

    boolean existsByUserName(String userName);

    Page<User> findAll(Pageable Pageable);

}

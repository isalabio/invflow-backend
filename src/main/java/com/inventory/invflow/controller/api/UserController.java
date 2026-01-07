package com.inventory.invflow.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventory.invflow.dto.user.UserResponse;
import com.inventory.invflow.enums.UserRole;
import com.inventory.invflow.service.UserService;


@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService userService;


    @GetMapping("/info/{userName}")
    @PreAuthorize("hasAnyRole('ADMIN') or #userName == authentication.name")
    public ResponseEntity<UserResponse> getUserInfo(@PathVariable String userName) {

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserByUserName(userName));
    }

    @PutMapping("/change-status/{userName}")
    public ResponseEntity<UserResponse> changeStatus (
        @PathVariable String userName,
        @RequestParam boolean enabled) {

        if(enabled){
            return ResponseEntity.status(HttpStatus.OK).body(userService.enable(userName));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(userService.disable(userName));
        }
    }

    @GetMapping("/role/{userName}")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable String userName,
            @RequestParam UserRole role) {

        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserRole(userName, role));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
        @RequestParam(defaultValue = "0") int page, 
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers(pageable));
    }

  
}

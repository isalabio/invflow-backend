package com.inventory.invflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class MyExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException ex) {
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<String> handleDuplicate(DuplicateResourceException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<String> handleAccountDisable(AccountDisabledException ex) {

        return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).body(ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<String> handleAccountLocked(AccountLockedException ex) {

        return ResponseEntity.status(HttpStatus.LOCKED).body(ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredential(BadCredentialsException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(LowStockException.class)
    public ResponseEntity<String> handleLowStock(LowStockException ex) {

        return ResponseEntity.status(HttpStatus.OK).body(ex.getMessage());
    }
}

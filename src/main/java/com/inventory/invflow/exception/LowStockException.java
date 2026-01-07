package com.inventory.invflow.exception;

public class LowStockException extends RuntimeException {
    
    public LowStockException(String message) {
        super(message);
    }
}

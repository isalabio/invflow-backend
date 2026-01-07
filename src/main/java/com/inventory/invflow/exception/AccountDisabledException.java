package com.inventory.invflow.exception;

public class AccountDisabledException extends RuntimeException {
    
    public AccountDisabledException(String message) {
        super(message);
    }
}

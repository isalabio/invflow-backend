package com.inventory.invflow.exception;

public class AccountLockedException extends RuntimeException{
    
    public AccountLockedException(String message) {
        super(message);
    }
}

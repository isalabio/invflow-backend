package com.inventory.invflow.enums;

public enum UserRole {
    ADMIN("管理員"),
    MANAGER("主管"),
    OPERATOR("專員"),
    VIEWER("訪客");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return this.displayName;
    }
}



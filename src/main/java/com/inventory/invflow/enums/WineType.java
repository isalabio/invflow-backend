package com.inventory.invflow.enums;

public enum WineType {
    RED("RE", "紅酒"), 
    WHITE("WH", "白酒"), 
    CHAMPAGNE("CH", "香檳"),
    SPARKLING("SP", "氣泡酒"),
    ROSE("RO", "粉紅酒"),
    WHISKY("WK", "威士忌"),
    ORANGE("OR", "橘酒");

    private final String code;
    private final String displayName;

    WineType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode(){
        return this.code;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}

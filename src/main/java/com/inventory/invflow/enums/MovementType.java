package com.inventory.invflow.enums;

public enum MovementType {
    PURCHASE("PURCHASE", "進貨", Direction.IN),
    TRANSFER_IN("TRANSFER_IN", "轉倉入", Direction.IN),
    RETURN_IN("RETURN_IN", "退貨入", Direction.IN),

    DAMAGE("DAMAGE", "損壞", Direction.OUT),
    LOST("LOST", "遺失", Direction.OUT),
    SCRAP("SCRAP", "報廢", Direction.OUT),
    TRANSFER_OUT("TRANSFER_OUT", "退倉出", Direction.OUT),

    SALE("SALE", "銷貨", Direction.OUT),
    MANUAL_ADJUST("MANUAL_ADJUST", "手動調整", Direction.NONE);

    private final String code;
    private final String displayName;
    private final Direction direction;

    MovementType(String code, String displayName, Direction direction){
        this.code = code;
        this.displayName = displayName;
        this.direction = direction;
    }

    public String getCode(){
        return this.code;
    }

    public String getDisplayName(){
        return this.displayName;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public boolean isIn() {
        return direction == Direction.IN;
    }

    public boolean isOut() {
        return direction == Direction.OUT;
    }

    public boolean isAllowedForCreateLog() {
        return this.direction != Direction.NONE;
    }

    public int toSignedQuantity(int qty) {

        if(qty <= 0) {
            throw new IllegalArgumentException("數量必須為正整數");
        }

        if(this.direction == Direction.NONE) {
            throw new IllegalArgumentException("此異動類型需走調整流程，不可直接建立");
        }

        return qty * direction.getSign();
    }

    public enum Direction {
        IN(+1),
        OUT(-1),
        NONE(0);

        private final int sign;

        Direction(int sign) {
            this.sign = sign;
        }

        public int getSign() {
            return sign;
        }    
        
        public boolean isNone() {
            return this == NONE;
        }
    }
 
}

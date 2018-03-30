package com.neuifo.appkit.model.modeltype;



/**
 * Created by neuifo on 2017/9/12.
 */

public enum BooleanEntity {

    TRUE(1), FALSE(0);

    private int val;

    BooleanEntity(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static BooleanEntity from(int i) {
        if (i == TRUE.getVal()) {
            return TRUE;
        }
        return FALSE;
    }

    public static BooleanEntity from(boolean value) {
        if (value == TRUE.getBooleanValue()) {
            return TRUE;
        }
        return FALSE;
    }

    public boolean getBooleanValue() {
        if (TRUE.equals(this)) {
            return true;
        }
        return false;
    }

    public int getId() {
        return val;
    }

}

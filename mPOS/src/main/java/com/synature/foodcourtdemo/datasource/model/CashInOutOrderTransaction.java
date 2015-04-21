package com.synature.foodcourtdemo.datasource.model;

/**
 * Created by j1tth4 on 19/3/2558.
 */
public class CashInOutOrderTransaction extends OrderTransaction{
    private int cashType;

    public int getCashType() {
        return cashType;
    }

    public void setCashType(int cashType) {
        this.cashType = cashType;
    }
}
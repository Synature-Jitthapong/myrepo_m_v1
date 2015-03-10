package com.synature.pos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by j1tth4 on 3/10/15.
 */
public class CashInOutProduct {
    @SerializedName("CashInOutID") private int cashInOutId;
    @SerializedName("CashInOutCode") private String cashInOutCode;
    @SerializedName("CashInOutName") private String cashInOutName;
    @SerializedName("CashInOutType") private int cashInOutType;
    @SerializedName("Deleted") private int deleted;
    @SerializedName("Ordering") private int ordering;

    public int getCashInOutId() {
        return cashInOutId;
    }

    public void setCashInOutId(int cashInOutId) {
        this.cashInOutId = cashInOutId;
    }

    public String getCashInOutCode() {
        return cashInOutCode;
    }

    public void setCashInOutCode(String cashInOutCode) {
        this.cashInOutCode = cashInOutCode;
    }

    public String getCashInOutName() {
        return cashInOutName;
    }

    public void setCashInOutName(String cashInOutName) {
        this.cashInOutName = cashInOutName;
    }

    public int getCashInOutType() {
        return cashInOutType;
    }

    public void setCashInOutType(int cashInOutType) {
        this.cashInOutType = cashInOutType;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }
}

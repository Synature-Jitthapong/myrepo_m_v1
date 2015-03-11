package com.synature.mpos.datasource.model;

import com.synature.mpos.datasource.SaleTransaction;

/**
 * Created by j1tth4 on 3/11/15.
 */
public class CashInOutOrderDetail extends SaleTransaction.SaleData_CashInOutDetail{
    private String productName;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}

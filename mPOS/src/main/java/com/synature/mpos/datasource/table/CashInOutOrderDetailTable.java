package com.synature.mpos.datasource.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by j1tth4 on 3/10/15.
 */
public class CashInOutOrderDetailTable {

    public static final String TABLE_CASH_INOUT_ORDER_DETAIL = "CashInOutOrderDetail";
    public static final String COLUMN_CASH_INOUT_PRICE = "cash_inout_price";

    public static final String SQL_CREATE = "create table " + TABLE_CASH_INOUT_ORDER_DETAIL + "(" +
            OrderDetailTable.COLUMN_ORDER_ID + " integer, " +
            OrderTransTable.COLUMN_TRANS_ID + " integer, " +
            ProductTable.COLUMN_PRODUCT_ID + " integer, " +
            COLUMN_CASH_INOUT_PRICE + " real, " +
            CashInOutProductTable.COLUMN_CASH_INOUT_TYPE + " integer, " +
            CashInOutOrderTransTable.COLUMN_STATUS_ID + " integer default 1, " +
            " primary key (" + OrderDetailTable.COLUMN_ORDER_ID + "))";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

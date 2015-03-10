package com.synature.mpos.datasource.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by j1tth4 on 3/10/15.
 */
public class CashInOutProductTable {
    public static final String TABLE_CASH_INOUT_PRODUCT = "CashInOutProduct";
    public static final String COLUMN_CASH_INOUT_ID = "cash_inout_id";
    public static final String COLUMN_CASH_INOUT_CODE = "cash_inout_code";
    public static final String COLUMN_CASH_INOUT_NAME = "cash_inout_name";
    public static final String COLUMN_CASH_INOUT_TYPE = "cash_inout_type";

    public static final String SQL_CREATE = "create table " + TABLE_CASH_INOUT_PRODUCT + " (" +
            COLUMN_CASH_INOUT_ID + " integer, " +
            COLUMN_CASH_INOUT_CODE + " text, " +
            COLUMN_CASH_INOUT_NAME + " text, " +
            COLUMN_CASH_INOUT_TYPE + " integer, " +
            BaseColumn.COLUMN_DELETED + " integer default 0," +
            BaseColumn.COLUMN_ORDERING + " integer default 0);";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_CASH_INOUT_PRODUCT);
        onCreate(db);
    }
}

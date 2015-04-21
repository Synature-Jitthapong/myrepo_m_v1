package com.synature.foodcourtdemo.datasource.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by j1tth4 on 2/26/15.
 */
public class CashInOutOrderTransTable {
    public static final String TABLE_CASH_INOUT_ORDER_TRANS = "CashInOutOrderTransaction";
    public static final String COLUMN_CASH_INOUT_DATE = "cash_inout_date";
    public static final String COLUMN_CASH_INOUT_DATE_TIME = "cash_inout_date_time";
    public static final String COLUMN_CASH_INOUT_TOTAL_PRICE = "cash_inout_total_price";
    public static final String COLUMN_STATUS_ID = "cash_inout_status_id";
    public static final String COLUMN_MOVEMENT = "movement";
    public static final String COLUMN_CASH_INOUT_NOTE = "cash_inout_note";

    private static final String SQL_CREATE = "create table " + TABLE_CASH_INOUT_ORDER_TRANS + "(" +
            OrderTransTable.COLUMN_TRANS_ID + " integer, " +
            ComputerTable.COLUMN_COMPUTER_ID + " integer, " +
            COLUMN_CASH_INOUT_DATE + " text, " +
            StaffTable.COLUMN_STAFF_ID + " integer, " +
            COLUMN_CASH_INOUT_DATE_TIME + " text, " +
            COLUMN_CASH_INOUT_TOTAL_PRICE + " real, " +
            COLUMN_STATUS_ID + " integer default 2, " +
            COLUMN_MOVEMENT + " integer default 0, " +
            COLUMN_CASH_INOUT_NOTE + " text, " +
            OrderTransTable.COLUMN_VOID_STAFF_ID + " integer, " +
            OrderTransTable.COLUMN_VOID_REASON + " text, " +
            OrderTransTable.COLUMN_VOID_TIME + " text, " +
            BaseColumn.COLUMN_UUID + " text, " +
            OrderTransTable.COLUMN_DOC_TYPE_ID + " integer default 9," +
            OrderTransTable.COLUMN_RECEIPT_YEAR + " integer, " +
            OrderTransTable.COLUMN_RECEIPT_MONTH + " integer, " +
            OrderTransTable.COLUMN_RECEIPT_ID + " integer, " +
            OrderTransTable.COLUMN_RECEIPT_NO + " text, " +
            SessionTable.COLUMN_SESS_ID + " integer, " +
            OrderTransTable.COLUMN_EJ + " text, " +
            OrderTransTable.COLUMN_EJ_VOID + " text, " +
            BaseColumn.COLUMN_SEND_STATUS + " integer default 0, " +
            " primary key (" +OrderTransTable.COLUMN_TRANS_ID + "))";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

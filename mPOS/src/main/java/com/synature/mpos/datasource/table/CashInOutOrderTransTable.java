package com.synature.mpos.datasource.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by j1tth4 on 2/26/15.
 */
public class CashInOutOrderTransTable {
    public static final String TABLE_CASHOUT_ORDER_TRANS = "CashInOutOrderTransaction";
    public static final String COLUMN_CASHOUT_DATE = "cash_out_date";
    public static final String COLUMN_CASHOUT_DATE_TIME = "cash_out_date_time";
    public static final String COLUMN_CASHOUT_TOTAL_PRICE = "cash_out_total_price";
    public static final String COLUMN_STATUS_ID = "cash_out_status_id";
    public static final String COLUMN_MOVEMENT = "movement";
    public static final String COLUMN_CASHOUT_NOTE = "cash_out_note";

    private static final String SQL_CREATE =
            "create table " + TABLE_CASHOUT_ORDER_TRANS + "(" +
                    OrderTransTable.COLUMN_TRANS_ID + " integer, " +
                    ComputerTable.COLUMN_COMPUTER_ID + " integer, " +
                    COLUMN_CASHOUT_DATE + " text, " +
                    StaffTable.COLUMN_STAFF_ID + " integer, " +
                    COLUMN_CASHOUT_DATE_TIME + " text, " +
                    COLUMN_CASHOUT_TOTAL_PRICE + " real, " +
                    COLUMN_STATUS_ID + " integer default "
            + ")";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

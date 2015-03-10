package com.synature.mpos.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.synature.mpos.Utils;
import com.synature.mpos.datasource.table.BaseColumn;
import com.synature.mpos.datasource.table.CashInOutOrderDetailTable;
import com.synature.mpos.datasource.table.CashInOutOrderTransTable;
import com.synature.mpos.datasource.table.CashInOutProductTable;
import com.synature.mpos.datasource.table.ComputerTable;
import com.synature.mpos.datasource.table.OrderDetailTable;
import com.synature.mpos.datasource.table.OrderTransTable;
import com.synature.mpos.datasource.table.SessionTable;
import com.synature.mpos.datasource.table.StaffTable;
import com.synature.pos.CashInOutProduct;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by j1tth4 on 3/10/15.
 */
public class CashInOutDataSource extends MPOSDatabase implements CashInOutDao{

    public static final int TRANS_VOID_STATUS = 5;
    public static final int ORDER_VOID_STATUS = 3;
    public static final int CASH_INOUT_DOC_TYPE = 9;

    public CashInOutDataSource(Context context) {
        super(context);
    }

    @Override
    public boolean deleteDetail(int transactionId, int detailId) {
        return false;
    }

    @Override
    public boolean updateDetail(int transactionId, int detailId, int qty) {
        return false;
    }

    @Override
    public int insertDetail(int transactionId, int productId, double price, int type) {
        return 0;
    }

    @Override
    public boolean voidTransaction(int transactionId, int computerId, int staffId, String reason) {
        return false;
    }

    @Override
    public boolean closeTransaction(int transactionId, int computerId, int staffId, double totalPrice) {
        return false;
    }

    @Override
    public boolean updateTransaction(int transactionId, int computerId, int staffId, String note) {
        return false;
    }

    @Override
    public int openTransaction(int computerId, int staffId, int sessionId, int movement) {
        int transactionId = getMaxTransactionId();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try{
            String uuid = getUUID();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sp = new SimpleDateFormat();
            sp.applyPattern(Utils.getISODateFormat());
            String date = sp.format(calendar.getTime());
            sp.applyPattern(Utils.getISODateTimeFormat());
            String dateTime = sp.format(calendar.getTime());
            ContentValues cv = new ContentValues();
            cv.put(OrderTransTable.COLUMN_TRANS_ID, transactionId);
            cv.put(ComputerTable.COLUMN_COMPUTER_ID, computerId);
            cv.put(CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE, date);
            cv.put(StaffTable.COLUMN_STAFF_ID, staffId);
            cv.put(CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE_TIME, dateTime);
            cv.put(CashInOutOrderTransTable.COLUMN_STATUS_ID, OrderTransDataSource.TRANS_STATUS_NEW);
            cv.put(CashInOutOrderTransTable.COLUMN_MOVEMENT, movement);
            cv.put(BaseColumn.COLUMN_UUID, uuid);
            cv.put(OrderTransTable.COLUMN_DOC_TYPE_ID, CASH_INOUT_DOC_TYPE);
            cv.put(OrderTransTable.COLUMN_RECEIPT_YEAR, calendar.get(Calendar.YEAR));
            cv.put(OrderTransTable.COLUMN_RECEIPT_MONTH, calendar.get(Calendar.MONTH) + 1);
            cv.put(SessionTable.COLUMN_SESS_ID, sessionId);
            db.insertOrThrow(CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS, null, cv);
            db.setTransactionSuccessful();
        }catch(SQLException e){
            transactionId = 0;
        } finally {
            db.endTransaction();
        }
        return transactionId;
    }

    @Override
    public SaleTransaction.SaleData_CashInOutDetail getCashInOutDetail(int transactionId, int detailId) {
        return null;
    }

    @Override
    public List<SaleTransaction.SaleData_CashInOutDetail> listAllCashInOutDetail(int transactionId) {
        return null;
    }

    @Override
    public SaleTransaction.SaleData_CashInOutTransaction getCashInOutTransaction(int transactionId) {
        return null;
    }

    @Override
    public List<SaleTransaction.SaleData_CashInOutTransaction> listCashInOutTransaction(String date) {
        return null;
    }

    @Override
    public CashInOutProduct getCashInOutProduct(int cashInOutId) {
        return null;
    }

    @Override
    public List<CashInOutProduct> listAllCashInOutProduct() {
        return null;
    }

    @Override
    public int insertCashInOut(List<CashInOutProduct> cashInOutProductList) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try{
            db.delete(CashInOutProductTable.TABLE_CASH_INOUT_PRODUCT, null, null);
            for(CashInOutProduct cp : cashInOutProductList) {
                ContentValues cv = new ContentValues();
                cv.put(CashInOutProductTable.COLUMN_CASH_INOUT_ID, cp.getCashInOutId());
                cv.put(CashInOutProductTable.COLUMN_CASH_INOUT_CODE, cp.getCashInOutCode());
                cv.put(CashInOutProductTable.COLUMN_CASH_INOUT_NAME, cp.getCashInOutName());
                cv.put(CashInOutProductTable.COLUMN_CASH_INOUT_TYPE, cp.getCashInOutType());
                cv.put(BaseColumn.COLUMN_DELETED, cp.getDeleted());
                cv.put(BaseColumn.COLUMN_ORDERING, cp.getOrdering());
                db.insertOrThrow(CashInOutProductTable.TABLE_CASH_INOUT_PRODUCT, null, cv);
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        return 0;
    }

    private int getMaxDetailId(int transactionId){
        int detailId = 0;
        String sql = "select max(" + OrderDetailTable.COLUMN_ORDER_ID + ") " +
                " from " + CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL +
                " where " + OrderTransTable.COLUMN_TRANS_ID + "=?";
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(transactionId)});
        if(cursor.moveToFirst()){
            detailId = cursor.getInt(0);
        }
        cursor.close();
        return detailId + 1;
    }

    private int getMaxTransactionId(){
        int transactionId = 0;
        String sql = "select max(" + OrderTransTable.COLUMN_TRANS_ID + ")" +
                " from " + CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(cursor.moveToFirst()){
            transactionId = cursor.getInt(0);
        }
        cursor.close();
        return transactionId + 1;
    }
}

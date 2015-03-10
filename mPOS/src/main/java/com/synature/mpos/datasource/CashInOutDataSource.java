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
import com.synature.mpos.datasource.table.ProductTable;
import com.synature.mpos.datasource.table.SessionTable;
import com.synature.mpos.datasource.table.StaffTable;
import com.synature.pos.CashInOutProduct;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by j1tth4 on 3/10/15.
 */
public class CashInOutDataSource extends MPOSDatabase implements CashInOutDao{

    public static final int TRANS_VOID_STATUS = 5;
    public static final int ORDER_VOID_STATUS = 3;
    public static final int CASH_INOUT_DOC_TYPE = 9;

    public static final String[] ALL_CASH_INOUT_TRANS_COLUMNS = {
            OrderTransTable.COLUMN_TRANS_ID,
            ComputerTable.COLUMN_COMPUTER_ID,
            CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE,
            CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE,
            StaffTable.COLUMN_STAFF_ID,
            CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE_TIME,
            CashInOutOrderTransTable.COLUMN_CASH_INOUT_TOTAL_PRICE,
            CashInOutOrderTransTable.COLUMN_STATUS_ID,
            CashInOutOrderTransTable.COLUMN_MOVEMENT,
            CashInOutOrderTransTable.COLUMN_CASH_INOUT_NOTE,
            OrderTransTable.COLUMN_VOID_STAFF_ID,
            OrderTransTable.COLUMN_VOID_REASON,
            OrderTransTable.COLUMN_VOID_TIME,
            BaseColumn.COLUMN_UUID,
            OrderTransTable.COLUMN_DOC_TYPE_ID,
            OrderTransTable.COLUMN_RECEIPT_YEAR,
            OrderTransTable.COLUMN_RECEIPT_MONTH,
            OrderTransTable.COLUMN_RECEIPT_ID,
            OrderTransTable.COLUMN_RECEIPT_NO,
            SessionTable.COLUMN_SESS_ID,
    };

    public static final String[] ALL_CASH_INOUT_DETAIL_COLUMNS = {
            OrderDetailTable.COLUMN_ORDER_ID,
            OrderTransTable.COLUMN_TRANS_ID,
            ProductTable.COLUMN_PRODUCT_ID,
            CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE,
            CashInOutProductTable.COLUMN_CASH_INOUT_TYPE,
            CashInOutOrderTransTable.COLUMN_STATUS_ID
    };

    public static final String[] ALL_CASH_INOUT_PRODUCT_COLUMNS = {
            CashInOutProductTable.COLUMN_CASH_INOUT_ID,
            CashInOutProductTable.COLUMN_CASH_INOUT_CODE,
            CashInOutProductTable.COLUMN_CASH_INOUT_NAME,
            CashInOutProductTable.COLUMN_CASH_INOUT_TYPE,
            BaseColumn.COLUMN_DELETED,
            BaseColumn.COLUMN_ORDERING
    };

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
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sp = new SimpleDateFormat();
        sp.applyPattern(Utils.getISODateTimeFormat());
        String dateTime = sp.format(calendar.getTime());
        int receiptId = getMaxReceiptId();
        String receiptNo = formatReceiptHeader(
                "",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                receiptId);
        ContentValues cv = new ContentValues();
        cv.put(CashInOutOrderTransTable.COLUMN_STATUS_ID, OrderTransDataSource.TRANS_STATUS_SUCCESS);
        cv.put(CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE_TIME, dateTime);
        cv.put(OrderTransTable.COLUMN_RECEIPT_ID, receiptId);
        cv.put(OrderTransTable.COLUMN_RECEIPT_NO, receiptNo);
        cv.put(CashInOutOrderTransTable.COLUMN_CASH_INOUT_TOTAL_PRICE, totalPrice);
        cv.put(StaffTable.COLUMN_STAFF_ID, staffId);
        getWritableDatabase().update(CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS,
                    cv,
                    OrderTransTable.COLUMN_TRANS_ID + "=?",
                    new String[]{
                            String.valueOf(transactionId)
                    });
        return true;
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
        SaleTransaction.SaleData_CashInOutDetail cashDetail = null;
        Cursor cursor = getReadableDatabase().query(CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL,
                ALL_CASH_INOUT_DETAIL_COLUMNS,
                OrderTransTable.COLUMN_TRANS_ID + "=?" +
                        " and " + OrderDetailTable.COLUMN_ORDER_ID + "=?",
                new String[]{
                        String.valueOf(transactionId),
                        String.valueOf(detailId)
                }, null, null, null);
        if(cursor.moveToFirst()){
            cashDetail = toCashDetail(cursor);
        }
        cursor.close();
        return cashDetail;
    }

    @Override
    public List<SaleTransaction.SaleData_CashInOutDetail> listAllCashInOutDetail(int transactionId) {
        List<SaleTransaction.SaleData_CashInOutDetail> cashDetailLst = null;
        Cursor cursor = getReadableDatabase().query(CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL,
                ALL_CASH_INOUT_DETAIL_COLUMNS,
                OrderTransTable.COLUMN_TRANS_ID + "=?",
                new String[]{
                        String.valueOf(transactionId)
                }, null, null, null);
        if(cursor.moveToFirst()){
            cashDetailLst = new ArrayList<SaleTransaction.SaleData_CashInOutDetail>();
            do{
                cashDetailLst.add(toCashDetail(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return cashDetailLst;
    }

    @Override
    public SaleTransaction.SaleData_CashInOutTransaction getCashInOutTransaction(int transactionId) {
        SaleTransaction.SaleData_CashInOutTransaction cashTrans = null;
        Cursor cursor = getReadableDatabase().query(CashInOutProductTable.TABLE_CASH_INOUT_PRODUCT,
                ALL_CASH_INOUT_TRANS_COLUMNS,
                OrderTransTable.COLUMN_TRANS_ID + "=?",
                new String[]{
                        String.valueOf(transactionId)
                }, null, null, null);
        if(cursor.moveToFirst()){
            cashTrans = toCashInOutTransaction(cursor);
        }
        cursor.close();
        return cashTrans;
    }

    @Override
    public List<SaleTransaction.SaleData_CashInOutTransaction> listCashInOutTransaction(String date) {
        List<SaleTransaction.SaleData_CashInOutTransaction> cashInOutTransactionsLst = null;
        Cursor cursor = getReadableDatabase().query(CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS,
                ALL_CASH_INOUT_TRANS_COLUMNS, CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE + "=?",
                new String[]{
                        date
                }, null, null, null);
        if(cursor.moveToFirst()){
            cashInOutTransactionsLst = new ArrayList<SaleTransaction.SaleData_CashInOutTransaction>();
            do{
                cashInOutTransactionsLst.add(toCashInOutTransaction(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return cashInOutTransactionsLst;
    }

    @Override
    public CashInOutProduct getCashInOutProduct(int cashInOutId) {
        return null;
    }

    @Override
    public List<CashInOutProduct> listAllCashInOutProduct() {
        List<CashInOutProduct> cashInOutLst = null;
        Cursor cursor = getReadableDatabase().query(
                CashInOutProductTable.TABLE_CASH_INOUT_PRODUCT,
                ALL_CASH_INOUT_PRODUCT_COLUMNS,
                BaseColumn.COLUMN_DELETED + "=?",
                new String[]{
                        String.valueOf(NOT_DELETE)
                }, null, null, COLUMN_ORDERING);
        if(cursor.moveToFirst()){
            cashInOutLst = new ArrayList<CashInOutProduct>();
            do{
                cashInOutLst.add(toCashInOutProduct(cursor));
            }while(cursor.moveToNext());
        }
        cursor.close();
        return cashInOutLst;
    }

    private SaleTransaction.SaleData_CashInOutTransaction toCashInOutTransaction(Cursor cursor){
        SaleTransaction.SaleData_CashInOutTransaction cashTrans =
                new SaleTransaction.SaleData_CashInOutTransaction();
        cashTrans.setiTransID(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_ID)));
        cashTrans.setiCompID(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
        cashTrans.setSzCashOutDate(cursor.getString(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE)));
        cashTrans.setiStaffID(cursor.getInt(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_ID)));
        cashTrans.setSzCashOutDateTime(cursor.getString(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE_TIME)));
        cashTrans.setfTotalPrice(cursor.getDouble(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_CASH_INOUT_TOTAL_PRICE)));
        cashTrans.setiStatusID(cursor.getInt(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_STATUS_ID)));
        cashTrans.setiMovement(cursor.getInt(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_MOVEMENT)));
        cashTrans.setSzCashOutNote(cursor.getString(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_CASH_INOUT_NOTE)));
        cashTrans.setiVoidStaffID(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_VOID_STAFF_ID)));
        cashTrans.setSzVoidReason(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_VOID_REASON)));
        cashTrans.setSzVoidDateTime(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_VOID_TIME)));
        cashTrans.setSzUDDID(cursor.getString(cursor.getColumnIndex(BaseColumn.COLUMN_UUID)));
        cashTrans.setiDocType(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_DOC_TYPE_ID)));
        cashTrans.setiReceiptYear(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_YEAR)));
        cashTrans.setiReceiptMonth(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_MONTH)));
        cashTrans.setiReceiptID(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_ID)));
        cashTrans.setSzReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_NO)));
        cashTrans.setiSessionID(cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
        return cashTrans;
    }

    private SaleTransaction.SaleData_CashInOutDetail toCashDetail(Cursor cursor){
        SaleTransaction.SaleData_CashInOutDetail cashDetail =
                new SaleTransaction.SaleData_CashInOutDetail();
        cashDetail.setiOrderID(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
        cashDetail.setiProductID(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
        cashDetail.setfCashOutPrice(cursor.getDouble(cursor.getColumnIndex(CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE)));
        cashDetail.setiCashOutType(cursor.getInt(cursor.getColumnIndex(CashInOutProductTable.COLUMN_CASH_INOUT_TYPE)));
        cashDetail.setiStatusID(cursor.getInt(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_STATUS_ID)));
        return cashDetail;
    }

    private CashInOutProduct toCashInOutProduct(Cursor cursor){
        CashInOutProduct cp = new CashInOutProduct();
        cp.setCashInOutId(cursor.getInt(cursor.getColumnIndex(CashInOutProductTable.COLUMN_CASH_INOUT_ID)));
        cp.setCashInOutCode(cursor.getString(cursor.getColumnIndex(CashInOutProductTable.COLUMN_CASH_INOUT_CODE)));
        cp.setCashInOutName(cursor.getString(cursor.getColumnIndex(CashInOutProductTable.COLUMN_CASH_INOUT_NAME)));
        cp.setCashInOutType(cursor.getInt(cursor.getColumnIndex(CashInOutProductTable.COLUMN_CASH_INOUT_TYPE)));
        return cp;
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

    private String formatReceiptHeader(String header, int year, int month, int day, int id){
        String receiptYear = String.format(Locale.US, "%04d", year);
        String receiptMonth = String.format(Locale.US, "%02d", month);
        String receiptDay = String.format(Locale.US, "%02d", day);
        String receiptId = String.format(Locale.US, "%04d", id);
        return header + receiptYear + receiptMonth + receiptDay + "/" + receiptId;
    }

    private int getMaxReceiptId(){
        int receiptId = 0;
        String sql = "select max(" + OrderTransTable.COLUMN_RECEIPT_ID + ")" +
                " from " + CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS +
                " where " + CashInOutOrderTransTable.COLUMN_STATUS_ID + "=?" +
                " order by date(" + CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE + ") desc limit 1";
        Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{
                    String.valueOf(OrderTransDataSource.TRANS_STATUS_SUCCESS)
                });
        return receiptId + 1;
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

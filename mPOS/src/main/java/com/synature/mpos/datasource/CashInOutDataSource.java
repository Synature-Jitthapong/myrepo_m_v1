package com.synature.mpos.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.synature.mpos.Utils;
import com.synature.mpos.datasource.model.CashInOutOrderDetail;
import com.synature.mpos.datasource.model.CashInOutOrderTransaction;
import com.synature.mpos.datasource.model.OrderTransaction;
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
            OrderTransTable.COLUMN_EJ,
            OrderTransTable.COLUMN_EJ_VOID
    };

    public static final String[] ALL_CASH_INOUT_DETAIL_COLUMNS = {
            OrderDetailTable.COLUMN_ORDER_ID,
            OrderTransTable.COLUMN_TRANS_ID,
            ProductTable.COLUMN_PRODUCT_ID,
            CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE,
            CashInOutProductTable.COLUMN_CASH_INOUT_TYPE,
            CashInOutOrderTransTable.COLUMN_STATUS_ID,
            CashInOutProductTable.COLUMN_CASH_INOUT_NAME
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
        getWritableDatabase().delete(CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL,
                OrderTransTable.COLUMN_TRANS_ID + "=? " +
                        " and " + OrderDetailTable.COLUMN_ORDER_ID + "=?",
                new String[]{
                        String.valueOf(transactionId),
                        String.valueOf(detailId)
                });
        return true;
    }

    @Override
    public int updateDetail(int transactionId, int detailId, double price) {
        int affectedRows = 0;
        ContentValues cv = new ContentValues();
        cv.put(CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE, price);
        affectedRows = getWritableDatabase().update(
                CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL,
                cv,
                OrderTransTable.COLUMN_TRANS_ID + "=?" +
                        " and " + OrderDetailTable.COLUMN_ORDER_ID + "=?",
                new String[]{
                        String.valueOf(transactionId),
                        String.valueOf(detailId)
                });
        return affectedRows;
    }

    @Override
    public int insertDetail(int transactionId, int productId, double price, int type) throws SQLException{
        int orderId = getMaxDetailId();
        ContentValues cv = new ContentValues();
        cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderId);
        cv.put(OrderTransTable.COLUMN_TRANS_ID, transactionId);
        cv.put(ProductTable.COLUMN_PRODUCT_ID, productId);
        cv.put(CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE, price);
        cv.put(CashInOutProductTable.COLUMN_CASH_INOUT_TYPE, type);
        getWritableDatabase().insertOrThrow(CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL,
                null, cv);
        return 0;
    }

    @Override
    public boolean voidTransaction(int transactionId, int computerId, int staffId, String reason) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sp = new SimpleDateFormat(Utils.getISODateTimeFormat());
        String dateTime = sp.format(calendar.getTime());
        ContentValues cv = new ContentValues();
        cv.put(CashInOutOrderTransTable.COLUMN_STATUS_ID, TRANS_VOID_STATUS);
        cv.put(OrderTransTable.COLUMN_VOID_STAFF_ID, staffId);
        cv.put(OrderTransTable.COLUMN_VOID_REASON, reason);
        cv.put(OrderTransTable.COLUMN_VOID_TIME, dateTime);
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try{
            String whereArgs[] = {
                    String.valueOf(transactionId)
            };
            db.update(CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS,
                    cv,
                    OrderTransTable.COLUMN_TRANS_ID + "=?",
                    whereArgs);
            cv = new ContentValues();
            cv.put(CashInOutOrderTransTable.COLUMN_STATUS_ID, ORDER_VOID_STATUS);
            db.update(CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL,
                    cv,
                    OrderTransTable.COLUMN_TRANS_ID + "=?",
                    whereArgs);
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        return true;
    }

    @Override
    public boolean closeTransaction(int transactionId, int computerId, int staffId,
                                    double totalPrice, String note) {
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
        cv.put(CashInOutOrderTransTable.COLUMN_CASH_INOUT_NOTE, note);
        getWritableDatabase().update(CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS,
                    cv,
                    OrderTransTable.COLUMN_TRANS_ID + "=?",
                    new String[]{
                            String.valueOf(transactionId)
                    });
        return true;
    }

    @Override
    public boolean cancelTransaction(int transactionId, int computerId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS,
                    CashInOutOrderTransTable.COLUMN_STATUS_ID + "=?",
                    new String[]{
                            String.valueOf(OrderTransDataSource.TRANS_STATUS_NEW)
                    });
            db.delete(CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL,
                    OrderTransTable.COLUMN_TRANS_ID + "=?",
                    new String[]{
                            String.valueOf(transactionId)
                    });
            db.setTransactionSuccessful();
        } finally{
            db.endTransaction();
        }
        return true;
    }

    @Override
    public boolean updateTransactionEj(int transactionId, int computerId, String ej) {
        ContentValues cv = new ContentValues();
        cv.put(OrderTransTable.COLUMN_EJ, ej);
        getWritableDatabase().update(
                CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS,
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
    public int openTransaction(int computerId, int staffId, int sessionId, int movement) throws SQLException{
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
    public int countCashInOutProduct() {
        int totalCashInOutProduct = 0;
        String sql = "select count(" + CashInOutProductTable.COLUMN_CASH_INOUT_ID + ")" +
                " from " + CashInOutProductTable.TABLE_CASH_INOUT_PRODUCT;
        Cursor cursor = getReadableDatabase().rawQuery(sql, null);
        if(cursor.moveToFirst()){
            totalCashInOutProduct = cursor.getInt(0);
        }
        cursor.close();
        return totalCashInOutProduct;
    }

    @Override
    public double getSummaryCashInOutAmount(String sessionDate) {
        double totalCashInOutAmount = 0;
        String sqlQuery = "select " +
                " sum(" + CashInOutOrderTransTable.COLUMN_CASH_INOUT_TOTAL_PRICE + " * " +
                CashInOutOrderTransTable.COLUMN_MOVEMENT + ")" +
                " from " + CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS +
                " where " + CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE + "=?" +
                " and " + CashInOutOrderTransTable.COLUMN_STATUS_ID + "=?";
        Cursor cursor = getReadableDatabase().rawQuery(
                sqlQuery,
                new String[]{
                        sessionDate,
                        String.valueOf(OrderTransDataSource.TRANS_STATUS_SUCCESS)
                });
        if(cursor.moveToFirst()){
            totalCashInOutAmount = cursor.getDouble(0);
        }
        cursor.close();
        return totalCashInOutAmount;
    }

    @Override
    public double getTotalCashAmount(int transactionId, int computerId) {
        double totalCashAmount = 0;
        String sql = "select sum(" + CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE + ")" +
                " from " + CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL +
                " where " + OrderTransTable.COLUMN_TRANS_ID + "=?";
        Cursor cursor = getReadableDatabase().rawQuery(
                sql,
                new String[]{
                    String.valueOf(transactionId)
                });
        if(cursor.moveToFirst()){
            totalCashAmount = cursor.getDouble(0);
        }
        cursor.close();
        return totalCashAmount;
    }

    @Override
    public List<CashInOutOrderDetail> listSummaryCashInOutDetail(String sessionDate) {
        List<CashInOutOrderDetail> summaryLst = null;
        String sqlQuery = "select c." + CashInOutProductTable.COLUMN_CASH_INOUT_NAME + ", " +
                " sum(b." + CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE + "*" +
                " b." + CashInOutProductTable.COLUMN_CASH_INOUT_TYPE + ")" +
                " as " + CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE +
                " from " + CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS + " a " +
                " left join " + CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL + " b " +
                " on a." + OrderTransTable.COLUMN_TRANS_ID + "=b." + OrderTransTable.COLUMN_TRANS_ID +
                " left join " + CashInOutProductTable.TABLE_CASH_INOUT_PRODUCT + " c " +
                " on b." + ProductTable.COLUMN_PRODUCT_ID + "=c." + CashInOutProductTable.COLUMN_CASH_INOUT_ID +
                " where a." + CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE + "=?" +
                " and a." + CashInOutOrderTransTable.COLUMN_STATUS_ID + "=?" +
                " group by c." + CashInOutProductTable.COLUMN_CASH_INOUT_ID;
        Cursor cursor = getReadableDatabase().rawQuery(
                sqlQuery,
                new String[]{
                        sessionDate,
                        String.valueOf(OrderTransDataSource.TRANS_STATUS_SUCCESS)
                }
        );
        if(cursor.moveToFirst()){
            summaryLst = new ArrayList<CashInOutOrderDetail>();
            do{
                CashInOutOrderDetail cashDetail = new CashInOutOrderDetail();
                cashDetail.setProductName(cursor.getString(cursor.getColumnIndex(CashInOutProductTable.COLUMN_CASH_INOUT_NAME)));
                cashDetail.setfCashOutPrice(cursor.getDouble(cursor.getColumnIndex(CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE)));
                summaryLst.add(cashDetail);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return summaryLst;
    }

    @Override
    public List<CashInOutOrderDetail> listAllCashInOutDetail(int transactionId) {
        List<CashInOutOrderDetail> cashDetailLst = null;
        String sql = "select a.*, b." + CashInOutProductTable.COLUMN_CASH_INOUT_NAME +
                " from " + CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL + " a " +
                " left join " + CashInOutProductTable.TABLE_CASH_INOUT_PRODUCT + " b " +
                " on a." + ProductTable.COLUMN_PRODUCT_ID + "=b." + CashInOutProductTable.COLUMN_CASH_INOUT_ID +
                " where a." + OrderTransTable.COLUMN_TRANS_ID + "=?" +
                " and a." + CashInOutOrderTransTable.COLUMN_STATUS_ID + "=?" ;
        Cursor cursor = getReadableDatabase().rawQuery(
                sql,
                new String[]{
                        String.valueOf(transactionId),
                        String.valueOf(OrderTransDataSource.TRANS_STATUS_NEW)
                });
        if(cursor.moveToFirst()){
            cashDetailLst = new ArrayList<CashInOutOrderDetail>();
            do{
                cashDetailLst.add(toCashDetail(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return cashDetailLst;
    }

    @Override
    public CashInOutOrderTransaction getCashInOutTransaction(int transactionId) {
        CashInOutOrderTransaction cashTrans = null;
        Cursor cursor = getReadableDatabase().query(
                CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS,
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
    public List<CashInOutOrderTransaction> listCashInOutTransaction(String date) {
        List<CashInOutOrderTransaction> cashInOutTransactionsLst = null;
        Cursor cursor = getReadableDatabase().query(
                CashInOutOrderTransTable.TABLE_CASH_INOUT_ORDER_TRANS,
                ALL_CASH_INOUT_TRANS_COLUMNS,
                CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE + "=?" +
                        " and " + CashInOutOrderTransTable.COLUMN_STATUS_ID + "=?",
                new String[]{
                        date,
                        String.valueOf(OrderTransDataSource.TRANS_STATUS_SUCCESS)
                }, null, null, null);
        if(cursor.moveToFirst()){
            cashInOutTransactionsLst = new ArrayList<CashInOutOrderTransaction>();
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
    public List<CashInOutProduct> listAllCashInOutProduct(int type) {
        List<CashInOutProduct> cashInOutLst = null;
        Cursor cursor = getReadableDatabase().query(
                CashInOutProductTable.TABLE_CASH_INOUT_PRODUCT,
                ALL_CASH_INOUT_PRODUCT_COLUMNS,
                CashInOutProductTable.COLUMN_CASH_INOUT_TYPE + "=?" +
                " and " + BaseColumn.COLUMN_DELETED + "=?",
                new String[]{
                        String.valueOf(type),
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

    private CashInOutOrderTransaction toCashInOutTransaction(Cursor cursor){
        CashInOutOrderTransaction cashTrans =
                new CashInOutOrderTransaction();
        cashTrans.setTransactionId(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_TRANS_ID)));
        cashTrans.setComputerId(cursor.getInt(cursor.getColumnIndex(ComputerTable.COLUMN_COMPUTER_ID)));
        cashTrans.setSaleDate(cursor.getString(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE)));
        cashTrans.setOpenStaffId(cursor.getInt(cursor.getColumnIndex(StaffTable.COLUMN_STAFF_ID)));
        cashTrans.setOpenTime(cursor.getString(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_CASH_INOUT_DATE_TIME)));
        cashTrans.setTransactionStatusId(cursor.getInt(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_STATUS_ID)));
        cashTrans.setTransactionNote(cursor.getString(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_CASH_INOUT_NOTE)));
        cashTrans.setVoidStaffId(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_VOID_STAFF_ID)));
        cashTrans.setVoidReason(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_VOID_REASON)));
        cashTrans.setVoidTime(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_VOID_TIME)));
        cashTrans.setReceiptYear(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_YEAR)));
        cashTrans.setReceiptMonth(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_MONTH)));
        cashTrans.setReceiptId(cursor.getInt(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_ID)));
        cashTrans.setReceiptNo(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_RECEIPT_NO)));
        cashTrans.setSessionId(cursor.getInt(cursor.getColumnIndex(SessionTable.COLUMN_SESS_ID)));
        cashTrans.setEj(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_EJ)));
        cashTrans.setEjVoid(cursor.getString(cursor.getColumnIndex(OrderTransTable.COLUMN_EJ_VOID)));
        return cashTrans;
    }

    private CashInOutOrderDetail toCashDetail(Cursor cursor){
        CashInOutOrderDetail cashDetail = new CashInOutOrderDetail();
        cashDetail.setiOrderID(cursor.getInt(cursor.getColumnIndex(OrderDetailTable.COLUMN_ORDER_ID)));
        cashDetail.setiProductID(cursor.getInt(cursor.getColumnIndex(ProductTable.COLUMN_PRODUCT_ID)));
        cashDetail.setfCashOutPrice(cursor.getDouble(cursor.getColumnIndex(CashInOutOrderDetailTable.COLUMN_CASH_INOUT_PRICE)));
        cashDetail.setiCashOutType(cursor.getInt(cursor.getColumnIndex(CashInOutProductTable.COLUMN_CASH_INOUT_TYPE)));
        cashDetail.setiStatusID(cursor.getInt(cursor.getColumnIndex(CashInOutOrderTransTable.COLUMN_STATUS_ID)));
        cashDetail.setProductName(cursor.getString(cursor.getColumnIndex(CashInOutProductTable.COLUMN_CASH_INOUT_NAME)));
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
                " where " + CashInOutOrderTransTable.COLUMN_STATUS_ID + "=?";
        Cursor cursor = getReadableDatabase().rawQuery(sql,
                new String[]{
                    String.valueOf(OrderTransDataSource.TRANS_STATUS_SUCCESS)
                });
        if(cursor.moveToFirst()){
            receiptId = cursor.getInt(0);
        }
        cursor.close();
        return receiptId + 1;
    }

    private int getMaxDetailId(){
        int detailId = 0;
        String sql = "select max(" + OrderDetailTable.COLUMN_ORDER_ID + ") " +
                " from " + CashInOutOrderDetailTable.TABLE_CASH_INOUT_ORDER_DETAIL;
        Cursor cursor = getReadableDatabase().rawQuery(
                sql, null);
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

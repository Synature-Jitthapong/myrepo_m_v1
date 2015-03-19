package com.synature.mpos.datasource;

import com.synature.mpos.datasource.model.CashInOutOrderDetail;
import com.synature.mpos.datasource.model.CashInOutOrderTransaction;
import com.synature.mpos.datasource.model.OrderTransaction;
import com.synature.pos.CashInOutProduct;

import java.util.List;

/**
 * Created by j1tth4 on 3/10/15.
 */
public interface CashInOutDao {
    public boolean deleteDetail(int transactionId, int detailId);
    public int updateDetail(int transactionId, int detailId, double price);
    public int insertDetail(int transactionId, int productId, double price, int type);
    public boolean voidTransaction(int transactionId, int computerId, int staffId, String reason);
    public boolean closeTransaction(int transactionId, int computerId,
                                    int staffId, double totalPrice, String note);
    public boolean cancelTransaction(int transactionId, int computerId);
    public boolean updateTransactionEj(int transactionId, int computerId, String ej);
    public boolean updateTransaction(int transactionId, int computerId, int staffId, String note);
    public int openTransaction(int computerId, int staffId, int sessionId, int movement);
    public SaleTransaction.SaleData_CashInOutDetail getCashInOutDetail(int transactionId,
                                                                       int detailId);
    public int countCashInOutProduct();
    public double getSummaryCashInOutAmount(String sessionDate);
    public double getTotalCashAmount(int transactionId, int computerId);
    public List<CashInOutOrderDetail> listSummaryCashInOutDetail(String sessionDate);
    public List<CashInOutOrderDetail> listAllCashInOutDetail(int transactionId);
    public CashInOutOrderTransaction getCashInOutTransaction(int transactionId);
    public List<CashInOutOrderTransaction> listCashInOutTransaction(String date);
    public CashInOutProduct getCashInOutProduct(int cashInOutId);
    public List<CashInOutProduct> listAllCashInOutProduct(int type);
    public int insertCashInOut(List<CashInOutProduct> cashInOutProductList);
}

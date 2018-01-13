package com.synature.mpos;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.synature.mpos.datasource.PrintReceiptLogDataSource;
import com.synature.util.Logger;

public class PrintReceipt extends AsyncTask<Void, Void, Void> {

    public static final String TAG = "PrintReceipt";

    public static final int NORMAL = 1;
    public static final int WASTE = 2;

    protected OnPrintReceiptListener mListener;

    protected PrintReceiptLogDataSource mPrintLog;
    protected Context mContext;

    private PrinterBase mPrinter;

    /**
     * @param context
     */
    public PrintReceipt(Context context, OnPrintReceiptListener listener) {
        mContext = context;
        mPrintLog = new PrintReceiptLogDataSource(context);
        mListener = listener;

        if (Utils.isInternalPrinterSetting(mContext)) {
            mPrinter = new WintecPrinter(mContext);
        } else {
            mPrinter = new EPSONPrinter(mContext);
        }
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        List<PrintReceiptLogDataSource.PrintReceipt> printLogLst = mPrintLog.listPrintReceiptLog();
        for (int i = 0; i < printLogLst.size(); i++) {
            PrintReceiptLogDataSource.PrintReceipt printReceipt = printLogLst.get(i);
            try {
                mPrinter.createTextForPrintReceipt(printReceipt.getTransactionId(), printReceipt.isCopy(), false);
                mPrinter.print();
                mPrintLog.deletePrintStatus(printReceipt.getPrintId(), printReceipt.getTransactionId());
            } catch (Exception e) {
                mPrintLog.updatePrintStatus(printReceipt.getPrintId(), printReceipt.getTransactionId(), PrintReceiptLogDataSource.PRINT_NOT_SUCCESS);
                Logger.appendLog(mContext,
                        MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME,
                        " Print receipt fail : " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null)
            mListener.onPrePrint();
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mListener != null)
            mListener.onPostPrint();
    }

    public interface OnPrintReceiptListener {
        void onPrePrint();

        void onPostPrint();
    }
}

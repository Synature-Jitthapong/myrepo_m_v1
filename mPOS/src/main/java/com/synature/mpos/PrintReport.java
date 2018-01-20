package com.synature.mpos;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

public class PrintReport extends AsyncTask<Void, Void, Void> {

    public static enum WhatPrint {
        SUMMARY_SALE,
        PRODUCT_REPORT,
        BILL_REPORT
    }

    ;

    private Context mContext;
    private WhatPrint mWhatPrint;
    private String mDateFrom;
    private String mDateTo;
    private int mSessionId;
    private int mStaffId;

    /**
     * @param context
     * @param whatPrint
     * @param sessionId
     * @param staffId
     * @param dateTo
     */
    public PrintReport(Context context, WhatPrint whatPrint, int sessionId, int staffId, String dateTo) {
        mContext = context;
        mWhatPrint = whatPrint;
        mSessionId = sessionId;
        mStaffId = staffId;
        mDateTo = dateTo;
    }

    /**
     * @param context
     * @param whatPrint
     * @param dateFrom
     * @param dateTo
     */
    public PrintReport(Context context, WhatPrint whatPrint, String dateFrom, String dateTo) {
        this(context, whatPrint, 0, 0, dateTo);
        mDateFrom = dateFrom;
        mDateTo = dateTo;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        PrinterBase printer;
        try {
            if (Utils.isInternalPrinterSetting(mContext)) {
                printer = new WintecPrinter(mContext);
                switch (mWhatPrint) {
                    case SUMMARY_SALE:
                        printer.createTextForPrintSummaryReport(mSessionId, mStaffId, mDateTo);
                        break;
                    case PRODUCT_REPORT:
                        printer.createTextForPrintSaleByProductReport(mDateFrom, mDateTo);
                        break;
                    case BILL_REPORT:
                        printer.createTextForPrintSaleByBillReport(mDateFrom, mDateTo);
                        break;
                }
            } else {
                printer = new EPSONPrinter(mContext);
                switch (mWhatPrint) {
                    case SUMMARY_SALE:
                        printer.createTextForPrintSummaryReport(mSessionId, mStaffId, mDateTo);
                        break;
                    case PRODUCT_REPORT:
                        printer.createTextForPrintSaleByProductReport(mDateFrom, mDateTo);
                        break;
                    case BILL_REPORT:
                        printer.createTextForPrintSaleByBillReport(mDateFrom, mDateTo);
                        break;
                }
            }
            if (TextUtils.isEmpty(printer.getTextToPrint()))
                printer.print();
        } catch (Exception e) {
        }
        return null;
    }
}

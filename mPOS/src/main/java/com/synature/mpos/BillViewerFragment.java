package com.synature.mpos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;

public class BillViewerFragment extends DialogFragment implements OnClickListener {

    public static final String TAG = BillViewerFragment.class.getSimpleName();

    public static final int CHECK_VIEW = 1;
    public static final int REPORT_VIEW = 2;
    public static final int RECEIPT = 3;
    public static final int WASTE = 4;

    private int mTransactionId;
    private int mViewMode;
    private int mBillType = RECEIPT;
    private boolean mIsCopy = false;

    private TextPrint mTextPrint;

    private ImageButton mBtnPrint;
    private CustomFontTextView mTextView;

    public static BillViewerFragment newInstance(int transactionId, int viewMode,
                                                 int billType, boolean isCopy) {
        BillViewerFragment f = new BillViewerFragment();
        Bundle b = new Bundle();
        b.putInt("transactionId", transactionId);
        b.putInt("viewMode", viewMode);
        b.putInt("billType", billType);
        b.putBoolean("isCopy", isCopy);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransactionId = getArguments().getInt("transactionId");
        mViewMode = getArguments().getInt("viewMode");
        mBillType = getArguments().getInt("billType");
        mIsCopy = getArguments().getBoolean("isCopy");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View content = inflater.inflate(R.layout.bill_viewer, null, false);
        mBtnPrint = (ImageButton) content.findViewById(R.id.btnPrint);
        mTextView = (CustomFontTextView) content.findViewById(R.id.textView1);

        mBtnPrint.setOnClickListener(this);

        mTextPrint = new TextPrint(getActivity());
        mTextPrint.setPrintNoPriceComment(true);
        if (mViewMode == CHECK_VIEW) {
            mTextPrint.createTextForPrintCheckReceipt(mTransactionId);
        } else if (mViewMode == REPORT_VIEW) {
            if (!mIsCopy) {
                mBtnPrint.setVisibility(View.GONE);
            }
            if (mBillType == RECEIPT) {
                mTextPrint.createTextForPrintReceipt(mTransactionId, mIsCopy, false);
            } else if (mBillType == WASTE) {
                mTextPrint.createTextForPrintWasteReceipt(mTransactionId, mIsCopy, false);
            }
        }
        mTextView.setText(mTextPrint.getTextToPrint());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);
        AlertDialog d = builder.create();
        WindowManager.LayoutParams params = d.getWindow().getAttributes();
        //params.gravity = Gravity.LEFT;
        d.getWindow().setAttributes(params);
        d.show();
        return d;
    }

    private class TextPrint extends PrinterBase {

        public TextPrint(Context context) {
            super(context);
        }

        @Override
        public void print() {

        }
    }

    private class PrintCheck extends PrintReceipt {

        /**
         * @param context
         * @param listener
         */
        public PrintCheck(Context context, OnPrintReceiptListener listener) {
            super(context, listener);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            PrinterBase printer;
            if (Utils.isInternalPrinterSetting(getActivity())) {
                printer = new WintecPrinter(getActivity());
            } else {
                printer = new EPSONPrinter(getActivity());
            }
            try {
                printer.mTextToPrint.append(mTextPrint.getTextToPrint());
                printer.print();
            } catch (Exception e) {
            }
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        PrintCheck printCheck = new PrintCheck(getActivity(), new PrintReceipt.OnPrintReceiptListener() {
            @Override
            public void onPrePrint() {
                mBtnPrint.setEnabled(false);
            }

            @Override
            public void onPostPrint() {
                mBtnPrint.setEnabled(true);
            }
        });
        printCheck.execute();
    }
}

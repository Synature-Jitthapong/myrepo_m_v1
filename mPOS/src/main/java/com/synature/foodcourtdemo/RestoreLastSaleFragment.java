package com.synature.foodcourtdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.synature.foodcourtdemo.datasource.MPOSDatabase;
import com.synature.foodcourtdemo.datasource.SaleTransaction;
import com.synature.foodcourtdemo.datasource.table.ComputerTable;
import com.synature.foodcourtdemo.datasource.table.OrderDetailTable;
import com.synature.foodcourtdemo.datasource.table.OrderTransTable;
import com.synature.foodcourtdemo.datasource.table.PaymentDetailTable;
import com.synature.foodcourtdemo.datasource.table.ProductTable;
import com.synature.pos.WebServiceResult;

import org.ksoap2.serialization.PropertyInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by j1tth4 on 2/26/15.
 */
public class RestoreLastSaleFragment extends DialogFragment {

    public static final String TAG = RestoreLastSaleFragment.class.getSimpleName();

    private int mShopId;
    private int mComputerId;
    private BackSaleTransaction mSaleTrans;

    private TextView mTvContent;
    private FrameLayout mProgressContent;

    public static RestoreLastSaleFragment newInstance(int shopId, int computerId){
        RestoreLastSaleFragment f = new RestoreLastSaleFragment();
        Bundle b = new Bundle();
        b.putInt("computerId", computerId);
        b.putInt("shopId", shopId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mComputerId = getArguments().getInt("computerId");
        mShopId = getArguments().getInt("shopId");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new LastSaleTransactionLoader(getActivity()));
        executor.shutdown();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View content = getActivity().getLayoutInflater().inflate(R.layout.restore_sale_fragment, null);
        mTvContent = (TextView) content.findViewById(R.id.tvContent);
        mProgressContent = (FrameLayout) content.findViewById(R.id.progressContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setPositiveButton(android.R.string.ok, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        Button btnOk = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnOk.setEnabled(false);
        return dialog;
    }

    private void rollbackSale(BackSaleTransaction backSaleTrans){
        MPOSDatabase helper = new MPOSDatabase(getActivity().getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            createTempTable(db);
            for(SaleTransaction.SaleData_SaleTransaction saleTrans : backSaleTrans.getxArySaleTransaction()){
                for(SaleTransaction.SaleTable_OrderDetail orderDetail : saleTrans.getxAryOrderDetail()){
                    ContentValues cv = createOrderDetailContentValues(orderDetail);


                    for(SaleTransaction.SaleTable_CommentInfo comment : orderDetail.getxListCommentInfo()){

                    }
                    for(SaleTransaction.SaleTable_ChildOrderType7 orderSet : orderDetail.getxListChildOrderSetLinkType7()){

                    }
                }
                for(SaleTransaction.SaleTable_OrderPromotion promotion : saleTrans.getxAryOrderPromotion()){

                }
            }
            db.setTransactionSuccessful();
        } finally{
            db.endTransaction();
        }
    }

    private ContentValues createOrderDetailContentValues(Object obj){
        SaleTransaction.SaleTable_OrderDetail orderDetail =
                (SaleTransaction.SaleTable_OrderDetail) obj;
        ContentValues cv = new ContentValues();
        cv.put(OrderDetailTable.COLUMN_ORDER_ID, orderDetail.getiOrderDetailID());
        cv.put(OrderTransTable.COLUMN_TRANS_ID, orderDetail.getiTransactionID());
        cv.put(ComputerTable.COLUMN_COMPUTER_ID, orderDetail.getiComputerID());
        cv.put(ProductTable.COLUMN_PRODUCT_ID, orderDetail.getiProductID());
        cv.put(ProductTable.COLUMN_PRODUCT_TYPE_ID, orderDetail.getiProductTypeID());
        cv.put(OrderDetailTable.COLUMN_ORDER_QTY, orderDetail.getfQty());
        cv.put(ProductTable.COLUMN_PRODUCT_PRICE, orderDetail.getfPricePerUnit());
        cv.put(OrderDetailTable.COLUMN_PRICE_OR_PERCENT, 2);
        cv.put(ProductTable.COLUMN_VAT_TYPE, orderDetail.getiVatType());
        cv.put(OrderDetailTable.COLUMN_TOTAL_VAT, orderDetail.getfTotalVatAmount());
        cv.put(OrderDetailTable.COLUMN_TOTAL_VAT_EXCLUDE, orderDetail.getfTotalVatAmount());
        cv.put(OrderDetailTable.COLUMN_MEMBER_DISCOUNT, orderDetail.getfMemberDiscountAmount());
        cv.put(OrderDetailTable.COLUMN_PRICE_DISCOUNT, orderDetail.getfPriceDiscountAmount());
        cv.put(OrderDetailTable.COLUMN_TOTAL_RETAIL_PRICE, orderDetail.getfRetailPrice());
        cv.put(OrderDetailTable.COLUMN_TOTAL_SALE_PRICE, orderDetail.getfSalePrice());
        return cv;
    }

    private int countTransaction(){
        int total = 0;
        MPOSDatabase helper = new MPOSDatabase(getActivity().getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(" + OrderTransTable.COLUMN_TRANS_ID + ")"
                + " from " + OrderTransTable.TABLE_ORDER_TRANS, null);
        if(cursor.moveToFirst()){
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    private void createTempTable(SQLiteDatabase db){
        db.execSQL("create table RollbackSaleTransaction as select * from " + OrderTransTable.TABLE_ORDER_TRANS + " where 0;");
        db.execSQL("create table RollbackOrderDetail as select * from " + OrderDetailTable.TABLE_ORDER + " where 0;");
        db.execSQL("create table RollbackPaymentDetail as select * from " + PaymentDetailTable.TABLE_PAYMENT_DETAIL + " where 0;");
    }

    public class LastSaleTransactionLoader extends MPOSServiceBase{

        public static final String METHOD = "WSmPOS_GenerateAllSaleTransBackToMPos";

        public LastSaleTransactionLoader(Context context) {
            super(context, METHOD, null);

            mProperty = new PropertyInfo();
            mProperty.setName(SHOP_ID_PARAM);
            mProperty.setValue(mShopId);
            mProperty.setType(int.class);
            mSoapRequest.addProperty(mProperty);

            mProperty = new PropertyInfo();
            mProperty.setName(COMPUTER_ID_PARAM);
            mProperty.setValue(mComputerId);
            mProperty.setType(int.class);
            mSoapRequest.addProperty(mProperty);

            mProperty = new PropertyInfo();
            mProperty.setName(SALE_DATE_PARAM);
            mProperty.setValue("2015-02-26");
            mProperty.setType(String.class);
            mSoapRequest.addProperty(mProperty);

            mProgressContent.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, result);
            mProgressContent.setVisibility(View.GONE);
            WebServiceResult ws;
            try {
                ws = toServiceObject(result);
                if(ws.getiResultID() == WebServiceResult.SUCCESS_STATUS){
                    Gson gson = new Gson();
                    mSaleTrans = gson.fromJson(ws.getSzResultData(), BackSaleTransaction.class);
                    if(mSaleTrans != null && mSaleTrans.getxArySaleTransaction() != null){
                        if(mSaleTrans.getxArySaleTransaction().size() > 0){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog dialog = (AlertDialog) getDialog();
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                    mTvContent.setText(R.string.restore_last_sale);
                                }
                            });
                        }
                    }
                }else{
                    getDialog().dismiss();
                }
            } catch (JsonSyntaxException e) {
            }
        }
    }

    public static class BackSaleTransaction extends SaleTransaction.POSData_EndDaySaleTransaction implements Parcelable {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

    }
}

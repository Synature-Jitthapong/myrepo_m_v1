package com.synature.mpos;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.synature.mpos.datasource.OrderTransDataSource;
import com.synature.mpos.datasource.PaymentDetailDataSource;
import com.synature.mpos.datasource.model.MPOSPaymentDetail;
import com.synature.mpos.datasource.model.OrderDetail;
import com.synature.pos.PrepaidCardInfo;
import com.synature.util.CreditCardParser;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class FoodCourtPayActivity extends ActionBarActivity{

    public static final String TAG = FoodCourtCardPay.class.getSimpleName();

    public static final int STATUS_READY_TO_USE = 1; 	//Ready to Use
    public static final int STATUS_INUSE = 2;			//In Use
    public static final int STATUS_BLACK_LIST = 3;		//BlackList
    public static final int STATUS_CANCEL = 4;			//Cancel
    public static final int STATUS_MISSING = 5;			//Missing

    private static int mShopId;
    private static int mComputerId;
    private static int mTransactionId;
    private static int mStaffId;

    private static double sTotalPoint;
    private static double sTotalPaid;
    private static double sTotalDue;

    private static double sCardBalance;
    private static double sCardBalanceBefore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_court_pay);

        Intent intent = getIntent();
        mShopId = intent.getIntExtra("shopId", 0);
        mComputerId = intent.getIntExtra("computerId", 0);
        mTransactionId = intent.getIntExtra("transactionId", 0);
        mStaffId = intent.getIntExtra("staffId", 0);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(
                    R.id.content, new FoodCourtCardPayFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class FoodCourtCardPayFragment extends Fragment implements Runnable{

        /*
         * is magnatic read state
         */
        private boolean mIsRead = false;

        /*
         * Thread for run magnetic reader listener
         */
        private Thread mMsrThread;

        private WintecMagneticReader mMsrReader;

        private EditText mTxtCardNo;
        private EditText mTxtCardAmount;
        private Button mBtnConfirm;
        private TextView mTvTotalPoint;
        private TextView mTvTotalPaid;
        private TextView mTvTotalDue;
        private TextView mTvCardStatus;
        private ProgressBar mCardProgress;

        private PaydetailAdapter mPaydetailAdapter;
        private RecyclerView mRcPayDetail;
        private RecyclerView.LayoutManager mRcLayoutManager;


        private ProgressDialog mProgressDialog;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mProgressDialog = new ProgressDialog(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.foodcourt_cardpay_fragment, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mTvTotalPoint = (TextView) view.findViewById(R.id.tvTotalPoint);
            mTvTotalPaid = (TextView) view.findViewById(R.id.tvTotalPaid);
            mTvTotalDue = (TextView) view.findViewById(R.id.tvTotalDue);
            mTvCardStatus = (TextView) view.findViewById(R.id.tvCardStatus);
            mTxtCardNo = (EditText) view.findViewById(R.id.txtCardNo);
            mTxtCardAmount = (EditText) view.findViewById(R.id.txtCardAmount);
            mBtnConfirm = (Button) view.findViewById(R.id.btnFcPayConfirm);
            mRcPayDetail = (RecyclerView) view.findViewById(R.id.rcPayDetail);
            mCardProgress = (ProgressBar) view.findViewById(R.id.cardProgress);

            mRcPayDetail.setHasFixedSize(true);
            mRcLayoutManager = new LinearLayoutManager(getActivity());
            mRcPayDetail.setLayoutManager(mRcLayoutManager);

            mBtnConfirm.setEnabled(false);
            mBtnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirm();
                }
            });
            mTxtCardNo.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if(event.getAction() != KeyEvent.ACTION_DOWN)
                        return true;

                    if(keyCode == KeyEvent.KEYCODE_ENTER){
                        String cardNo = ((EditText) view).getText().toString();
                        if(!cardNo.isEmpty()){
                //          InputMethodManager imm = (InputMethodManager)
                //                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //          imm.hideSoftInputFromWindow(mTxtCardNo.getWindowToken(), 0);
                            loadCardInfo();
                        }
                        ((EditText) view).setText(null);
                    }
                    return false;
                }
            });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            summary();
            loadPayDetail();
        }

        private void summary(){
            OrderTransDataSource trans = new OrderTransDataSource(getActivity());
            OrderDetail sumOrder = trans.getSummaryOrder(mTransactionId, true);
            sTotalPoint = sumOrder.getTotalSalePrice() + sumOrder.getVatExclude();
            mTvTotalPoint.setText(Utils.currencyFormat(sTotalPoint));
        }

        private void loadPayDetail(){
            PaymentDetailDataSource payment = new PaymentDetailDataSource(getActivity());
            List<MPOSPaymentDetail> payLst = payment.listPayment(mTransactionId);
            if(mPaydetailAdapter == null){
                mPaydetailAdapter = new PaydetailAdapter(payLst);
                mRcPayDetail.setAdapter(mPaydetailAdapter);
            }else {
                mPaydetailAdapter.notifyDataSetChanged();
            }
            double totalPaid = payment.getTotalPayAmount(mTransactionId, true);
            sTotalDue = sTotalPoint - totalPaid;
            mTvTotalPaid.setText(Utils.currencyFormat(totalPaid));
            if(sTotalDue < 0)
                sTotalDue = 0.0d;
            mTvTotalDue.setText(Utils.currencyFormat(sTotalDue));
        }

        private void addPayment(int payTypeId, String cardNo, String remark){
            if(sTotalPaid > 0 && sTotalDue > 0){
                PaymentDetailDataSource payment = new PaymentDetailDataSource(getActivity());
                payment.addPaymentDetail(mTransactionId, mComputerId, payTypeId, sTotalPaid,
                        sTotalPaid >= sTotalDue ? sTotalDue : sTotalPaid, cardNo,
                        0, 0, 0, 0, remark);
                loadPayDetail();
                // display pay type to customer display
                if(Utils.isEnableWintecCustomerDisplay(getActivity())){
                    WintecCustomerDisplay dsp = new WintecCustomerDisplay(getActivity());
                    dsp.displayPayment(payment.getPaymentTypeName(payTypeId), Utils.currencyFormat(sTotalPaid));
                }
            }
        }

        private void setResultAndFinish(int printType){
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("printType", printType);
            intent.putExtra("totalSalePrice", sTotalPoint);
            intent.putExtra("totalPaid", sTotalPaid);
            intent.putExtra("change", 0);
            intent.putExtra("transactionId", mTransactionId);
            intent.putExtra("staffId", mStaffId);
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();
        }

        private void confirm(){
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new FoodCourtCardPay(getActivity(), mShopId, mComputerId, mStaffId,
                    mTxtCardNo.getText().toString(), NumberFormat.getInstance().format(sTotalPaid),
                    new CardPayReceiver(new Handler())));
            executor.shutdown();
        }

        @Override
        public void onStart() {
            super.onStart();
            try {
                mMsrReader = new WintecMagneticReader(getActivity());
                mMsrThread = new Thread(this);
                mMsrThread.start();
                mIsRead = true;
                Log.i(TAG, "Start magnetic reader thread");
            } catch (Exception e) {
                Log.e(TAG, "Error start magnetic reader thread " +
                                e.getMessage());
            }
        }

        @Override
        public void onStop() {
            closeMsrThread();
            mIsRead = false;
            mMsrReader.close();
            super.onStop();
        }

        /*
         * Close magnetic reader thread
         */
        private synchronized void closeMsrThread(){
            if(mMsrThread != null){
                mMsrThread.interrupt();
                mMsrThread = null;
            }
        }

        @Override
        public void run() {
            while(mIsRead){
                try {
                    final String content = mMsrReader.getTrackData();
                    if(content.length() > 0){
                        Log.i(TAG, "Content : " + content);
                        getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    CreditCardParser parser = new CreditCardParser();
                                    if (parser.parser(content)) {
                                        String cardNo = parser.getCardNo();
                                        mTxtCardNo.setText(null);
                                        mTxtCardNo.setText(cardNo);
                                        loadCardInfo();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                            }

                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, " Error when read data from magnetic card : " + e.getMessage());
                }
            }
        }

        private void loadCardInfo(){
            if(!TextUtils.isEmpty(mTxtCardNo.getText())){
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new FoodCourtBalanceOfCard(getActivity(), mShopId, mComputerId,
                        mStaffId, mTxtCardNo.getText().toString(),
                        new CardBalanceReceiver(new Handler())));
                executor.shutdown();
            }else{
                mTxtCardNo.requestFocus();
            }
        }

        private void setProgressStatus(int visible, String statusText){
            mCardProgress.setVisibility(visible);
            mTvCardStatus.setText(statusText);
        }

        private class CardBalanceReceiver extends ResultReceiver{

            /**
             * Create a new ResultReceive to receive results.  Your
             * {@link #onReceiveResult} method will be called from the thread running
             * <var>handler</var> if given, or from an arbitrary thread if null.
             *
             * @param handler
             */
            public CardBalanceReceiver(Handler handler) {
                super(handler);
                setProgressStatus(View.VISIBLE, getString(R.string.please_wait));
            }

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                setProgressStatus(View.GONE, null);
                switch(resultCode){
                    case FoodCourtMainService.RESULT_SUCCESS:
                        PrepaidCardInfo cardInfo = resultData.getParcelable("cardInfo");
                        if(cardInfo != null){
                            sCardBalance = cardInfo.getfCurrentAmount();
                            sCardBalanceBefore = sCardBalance;
                            sTotalPaid = sTotalPoint;
                            mTxtCardNo.setText(cardInfo.getSzCardNo());
                            mTxtCardAmount.setText(Utils.currencyFormat(sCardBalance));
                            if(cardInfo.getiCardStatus() == STATUS_READY_TO_USE) {
                                if (sCardBalance < sTotalPoint) {
                                    sTotalPaid = sCardBalance;
                                    mTxtCardAmount.setTextColor(Color.RED);
                                    mBtnConfirm.setEnabled(false);
                                } else {
                                    mTxtCardAmount.setTextColor(Color.BLACK);
                                    mBtnConfirm.setEnabled(true);
                                }
                                addPayment(PaymentDetailDataSource.PAY_TYPE_CASH,
                                        cardInfo.getSzCardNo(), "Food court payment");
                            }else{
                                int status = cardInfo.getiCardStatus();
                                if(status == STATUS_BLACK_LIST) {
                                    setProgressStatus(View.GONE, "Card in blacklist");
                                }else if(status == STATUS_CANCEL){
                                    setProgressStatus(View.GONE, "Card has been canceled");
                                }else if(status == STATUS_INUSE){
                                    setProgressStatus(View.GONE, "Card is in used");
                                }else if(status == STATUS_MISSING) {
                                    setProgressStatus(View.GONE, "Card is missing");
                                }
                            }
                        }
                        break;
                    case FoodCourtMainService.RESULT_ERROR:
                        new AlertDialog.Builder(getActivity())
                            .setMessage(resultData.getString("msg"))
                            .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                        break;
                }
            }
        }

        private class CardPayReceiver extends ResultReceiver{

            /**
             * Create a new ResultReceive to receive results.  Your
             * {@link #onReceiveResult} method will be called from the thread running
             * <var>handler</var> if given, or from an arbitrary thread if null.
             *
             * @param handler
             */
            public CardPayReceiver(Handler handler) {
                super(handler);
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.show();
            }

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                switch (resultCode){
                    case FoodCourtMainService.RESULT_SUCCESS:
                        if(mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                        PrepaidCardInfo cardInfo = resultData.getParcelable("cardInfo");
                        if(cardInfo != null){
                            sCardBalance = sCardBalanceBefore - sTotalPoint;
                            OrderTransDataSource trans = new OrderTransDataSource(getActivity());
                            trans.closeTransaction(mTransactionId, mStaffId, sTotalPoint, "");
                            trans.updateTransactionPoint(mTransactionId, sCardBalanceBefore, sCardBalance);

                            PaymentDetailDataSource payment = new PaymentDetailDataSource(getActivity());
                            payment.confirmPayment(mTransactionId);
                            setResultAndFinish(PrintReceipt.NORMAL);
                        }
                        break;
                    case FoodCourtMainService.RESULT_ERROR:
                        if(mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        mBtnConfirm.setEnabled(true);
                        new AlertDialog.Builder(getActivity())
                                .setMessage(resultData.getString("msg"))
                                .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).show();
                        break;
                }
            }
        }

        public class PaydetailAdapter extends RecyclerView.Adapter<PaydetailAdapter.ViewHolder>{

            private List<MPOSPaymentDetail> mPayDetailLst;

            public class ViewHolder extends RecyclerView.ViewHolder{

                public TextView mTvFcPayNo;
                public TextView mTvFcCardNo;
                public TextView mTvFcCardPayAmount;

                public ViewHolder(View itemView) {
                    super(itemView);
                    mTvFcPayNo = (TextView) itemView.findViewById(R.id.tvFcPayNo);
                    mTvFcCardNo = (TextView) itemView.findViewById(R.id.tvFcPayCardNo);
                    mTvFcCardPayAmount = (TextView) itemView.findViewById(R.id.tvFcPayAmount);
                }
            }

            public PaydetailAdapter(List<MPOSPaymentDetail> payDetailLst){
                mPayDetailLst = payDetailLst;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.foodcourt_cardpay_item, parent, false);
                ViewHolder holder = new ViewHolder(v);
                return holder;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                holder.mTvFcPayNo.setText(String.valueOf(position + 1));
                holder.mTvFcCardNo.setText(mPayDetailLst.get(position).getCreditCardNo());
                holder.mTvFcCardPayAmount.setText(Utils.currencyFormat(mPayDetailLst.get(position).getPayAmount()));
            }

            @Override
            public int getItemCount() {
                return mPayDetailLst != null ? mPayDetailLst.size() : 0;
            }
        }
    }
}

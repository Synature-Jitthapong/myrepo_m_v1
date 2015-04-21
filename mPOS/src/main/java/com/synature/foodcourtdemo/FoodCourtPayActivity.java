package com.synature.foodcourtdemo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.ResultReceiver;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.synature.foodcourtdemo.datasource.OrderTransDataSource;
import com.synature.foodcourtdemo.datasource.PaymentDetailDataSource;
import com.synature.foodcourtdemo.datasource.model.MPOSPaymentDetail;
import com.synature.foodcourtdemo.datasource.model.OrderDetail;
import com.synature.util.CreditCardParser;

import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FoodCourtPayActivity extends Activity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_court_pay);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mShopId = intent.getIntExtra("shopId", 0);
        mComputerId = intent.getIntExtra("computerId", 0);
        mTransactionId = intent.getIntExtra("transactionId", 0);
        mStaffId = intent.getIntExtra("staffId", 0);

        if(savedInstanceState == null){
            getFragmentManager().beginTransaction().replace(
                    R.id.content, new FoodCourtCardPayFragment()).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                cancel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cancel(){
        PaymentDetailDataSource payment = new PaymentDetailDataSource(this);
        payment.deleteAllPaymentDetail(mTransactionId);
        finish();
    }

    public static class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{
                android.R.attr.listDivider
        };

        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

        private Drawable mDivider;

        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
            final TypedArray a = context.obtainStyledAttributes(ATTRS);
            mDivider = a.getDrawable(0);
            a.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                        .getLayoutParams();
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
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

        private List<MPOSPaymentDetail> mPayDetailLst;

        private double mTotalPoint;
        private double mTotalPaid;
        private double mTotalDue;

        private EditText mTxtCardNo;
        private EditText mTxtCardAmount;
        private Button mBtnConfirm;
        private Button mBtnCancel;
        private ImageButton mBtnSearchCard;
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
            mBtnCancel = (Button) view.findViewById(R.id.btnFcCancel);
            mRcPayDetail = (RecyclerView) view.findViewById(R.id.rcPayDetail);
            mCardProgress = (ProgressBar) view.findViewById(R.id.cardProgress);
            mBtnSearchCard = (ImageButton) view.findViewById(R.id.btnSearchCard);

            mRcPayDetail.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
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
            mBtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FoodCourtPayActivity) getActivity()).cancel();
                }
            });
            mBtnSearchCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cardNo = mTxtCardNo.getText().toString();
                    if(!TextUtils.isEmpty(cardNo)){
                        loadCardInfo();
                    }
                }
            });
            mTxtCardNo.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent event) {
                    if(event.getAction() != KeyEvent.ACTION_DOWN)
                        return true;

                    if(keyCode == KeyEvent.KEYCODE_ENTER){
                        String cardNo = ((EditText) view).getText().toString();
                        if(!TextUtils.isEmpty(cardNo)){
                            loadCardInfo();
                        }
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
            mTotalPoint = sumOrder.getTotalSalePrice() + sumOrder.getVatExclude();
            mTvTotalPoint.setText(Utils.currencyFormat(mTotalPoint));
        }

        private void loadPayDetail(){
            PaymentDetailDataSource payment = new PaymentDetailDataSource(getActivity());
            mPayDetailLst = payment.listFcPayment(mTransactionId, true);
            if(mPaydetailAdapter == null){
                mPaydetailAdapter = new PaydetailAdapter();
                mRcPayDetail.setAdapter(mPaydetailAdapter);
            }
            mPaydetailAdapter.notifyDataSetChanged();
            mTotalPaid = payment.getTotalPayAmount(mTransactionId, true);
            mTotalDue = mTotalPoint - mTotalPaid;
            mTvTotalPaid.setText(Utils.currencyFormat(mTotalPaid));
            if(mTotalPaid >= mTotalPoint){
                mBtnConfirm.setEnabled(true);
            }else{
                mBtnConfirm.setEnabled(false);
            }
            if(mTotalDue < 0) {
                mTotalDue = 0.0d;
            }
            mTvTotalDue.setText(Utils.currencyFormat(mTotalDue));
        }

        private void addPayment(int payTypeId, double paidAmount, String cardNo, String remark){
            if(paidAmount > 0 && mTotalDue > 0){
                PaymentDetailDataSource payment = new PaymentDetailDataSource(getActivity());
                boolean isAdded = payment.checkAddedFcPayment(mTransactionId, cardNo);
                if(!isAdded) {
                    paidAmount = paidAmount >= mTotalDue ? mTotalDue : paidAmount;
                    payment.addFcPaymentDetail(mTransactionId, mComputerId, payTypeId, paidAmount,
                            paidAmount, cardNo, 0, 0, 0, 0, remark);
                    loadPayDetail();
                    // display pay type to customer display
                    if (Utils.isEnableWintecCustomerDisplay(getActivity())) {
                        WintecCustomerDisplay dsp = new WintecCustomerDisplay(getActivity());
                        dsp.displayPayment(payment.getPaymentTypeName(payTypeId), Utils.currencyFormat(paidAmount));
                    }
                }else{
                    new AlertDialog.Builder(getActivity())
                            .setMessage("This card is already used. Please use another card.")
                            .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .show();
                }
            }
        }

        private void setResultAndFinish(int printType){
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("printType", printType);
            intent.putExtra("totalSalePrice", mTotalPoint);
            intent.putExtra("totalPaid", mTotalPaid);
            intent.putExtra("change", 0);
            intent.putExtra("transactionId", mTransactionId);
            intent.putExtra("staffId", mStaffId);
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();
        }

        private void confirm(){
            List<MPOSPaymentDetail> paymentLst = mPayDetailLst;
            if(paymentLst != null && paymentLst.size() > 0){
                String cardsNo = "";
                String cardsPayAmount = "";
                for(int i = 0; i < paymentLst.size(); i++){
                    MPOSPaymentDetail payment = paymentLst.get(i);
                    cardsNo += payment.getCreditCardNo();
                    cardsPayAmount += NumberFormat.getInstance().format(payment.getPayAmount());
                    if( i < paymentLst.size() -1){
                        cardsNo += ",";
                        cardsPayAmount += ",";
                    }
                }
                Log.e("CardList", cardsNo);
                Log.e("CardPayList", cardsPayAmount);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new FoodCourtCardPay(getActivity(), mShopId, mComputerId, mStaffId,
                        cardsNo, cardsPayAmount, new CardPayReceiver(new Handler())));
                executor.shutdown();
            }
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
            mMsrReader.close();
        }

        private void loadCardInfo(){
            if(!TextUtils.isEmpty(mTxtCardNo.getText())){
                InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mTxtCardNo.getWindowToken(), 0);
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
                        String cardNo = resultData.getString("cardNo");
                        double cardBalance = resultData.getDouble("cardBalance");
                        double paidAmount = mTotalPoint;
                        mTxtCardAmount.setText(Utils.currencyFormat(cardBalance));
                        if (cardBalance < mTotalPoint) {
                            paidAmount = cardBalance;
                            mTxtCardAmount.setTextColor(Color.RED);
                        } else {
                            mTxtCardAmount.setTextColor(Color.BLACK);
                        }
                        addPayment(PaymentDetailDataSource.PAY_TYPE_CASH, paidAmount,
                                cardNo, cardNo);
                        break;
                    case FoodCourtMainService.RESULT_ERROR:
                        mTxtCardNo.setText(null);
                        mTxtCardAmount.setText(null);
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
                        double cardBalance = resultData.getDouble("cardBalance");
                        OrderTransDataSource trans = new OrderTransDataSource(getActivity());
                        trans.updateTransactionPoint(mTransactionId, 0, cardBalance, mTxtCardNo.getText().toString());
                        trans.closeTransaction(mTransactionId, mStaffId, mTotalPoint, Utils.getISODate());

                        PaymentDetailDataSource payment = new PaymentDetailDataSource(getActivity());
                        payment.confirmPayment(mTransactionId);
                        setResultAndFinish(PrintReceipt.NORMAL);
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

            public class ViewHolder extends RecyclerView.ViewHolder{

                public TextView mTvFcPayNo;
                public TextView mTvFcCardNo;
                public TextView mTvFcCardPayAmount;
                public ImageButton mBtnFcCardPayDelete;

                public ViewHolder(View itemView) {
                    super(itemView);
                    mTvFcPayNo = (TextView) itemView.findViewById(R.id.tvFcPayNo);
                    mTvFcCardNo = (TextView) itemView.findViewById(R.id.tvFcPayCardNo);
                    mTvFcCardPayAmount = (TextView) itemView.findViewById(R.id.tvFcPayAmount);
                    mBtnFcCardPayDelete = (ImageButton) itemView.findViewById(R.id.btnFcPayDelete);
                    mBtnFcCardPayDelete.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.foodcourt_cardpay_item, parent, false);
                ViewHolder holder = new ViewHolder(v);
                return holder;
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, final int position) {
                holder.mTvFcPayNo.setText(String.valueOf(position + 1));
                holder.mTvFcCardNo.setText(mPayDetailLst.get(position).getCreditCardNo());
                holder.mTvFcCardPayAmount.setText(Utils.currencyFormat(mPayDetailLst.get(position).getPayAmount()));
                holder.mBtnFcCardPayDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getActivity())
                                .setMessage(R.string.confirm_delete_item)
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {}
                                })
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PaymentDetailDataSource payment = new PaymentDetailDataSource(getActivity());
                                        payment.deleteFcPaymentDetail(mTransactionId, mPayDetailLst.get(position).getPaymentDetailId());
                                        loadPayDetail();
                                    }
                                }).show();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mPayDetailLst != null ? mPayDetailLst.size() : 0;
            }
        }
    }
}

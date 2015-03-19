package com.synature.mpos;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.synature.mpos.datasource.CashInOutDao;
import com.synature.mpos.datasource.CashInOutDataSource;
import com.synature.mpos.datasource.model.CashInOutOrderTransaction;
import com.synature.mpos.datasource.model.OrderTransaction;

import java.util.List;


public class ReprintCashInOutActivity extends Activity {

    public static final String TAG = ReprintCashInOutActivity.class.getSimpleName();

    private CashInOutDao mCashInOutDao;
    private List<CashInOutOrderTransaction> mCashInOutTransLst;
    private CashInOutBillAdapter mCashInOutBillAdapter;

    private ListView mLvCashInOutTrans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = 500;
        params.height= 500;
        params.alpha = 1.0f;
        params.dimAmount = 0.5f;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_reprint_cash_in_out);
        mLvCashInOutTrans = (ListView) findViewById(R.id.lvCashInOutBill);

        mCashInOutDao = new CashInOutDataSource(this);
        setupCashInOutAdapter();
    }

    private void setupCashInOutAdapter(){
        loadCashInOutTrans();
        if(mCashInOutBillAdapter == null){
            mCashInOutBillAdapter = new CashInOutBillAdapter();
            mLvCashInOutTrans.setAdapter(mCashInOutBillAdapter);
        }else{
            mCashInOutBillAdapter.notifyDataSetChanged();
        }
    }

    private void loadCashInOutTrans(){
        mCashInOutTransLst = mCashInOutDao.listCashInOutTransaction(Utils.getISODate());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CashInOutBillAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mCashInOutTransLst != null ? mCashInOutTransLst.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mCashInOutTransLst.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.reprint_trans_item, parent, false);
                holder = new ViewHolder();
                holder.tvNo = (TextView) convertView.findViewById(R.id.tvNo);
                holder.tvReceiptNo = (TextView) convertView.findViewById(R.id.tvReceiptNo);
                holder.tvAd = (TextView) convertView.findViewById(R.id.tvAd);
                holder.btnPrint = (ImageButton) convertView.findViewById(R.id.btnPrint);
                holder.btnBillDetail = (ImageButton) convertView.findViewById(R.id.btnBillDetail);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            final CashInOutOrderTransaction trans = mCashInOutTransLst.get(position);
            holder.tvNo.setText(String.valueOf(position + 1) + ".");
            holder.tvReceiptNo.setText(trans.getReceiptNo());
            if(!TextUtils.isEmpty(trans.getPaidTime())){
                holder.tvAd.setText(Utils.timeFormat(Utils.convertISODateTimeToCalendar(trans.getPaidTime())));
            }
            holder.btnPrint.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    holder.btnPrint.setEnabled(false);
                    new Reprint(trans.getTransactionId(), trans.getCashType(), holder.btnPrint).execute();
                }

            });
            holder.btnPrint.setBackgroundResource(R.drawable.btn_holo_gray);
            holder.btnBillDetail.setVisibility(View.GONE);
            return convertView;
        }

        class ViewHolder {
            TextView tvNo;
            TextView tvReceiptNo;
            TextView tvAd;
            ImageButton btnPrint;
            ImageButton btnBillDetail;
        }
    }

    private class Reprint extends PrintReceipt{

        private int transactionId;
        private int cashType;
        private ImageButton mBtnPrint;

        public Reprint(int transactionId, int cashType, ImageButton refBtnPrint) {
            super(ReprintCashInOutActivity.this, null);
            this.transactionId = transactionId;
            this.cashType = cashType;
            mBtnPrint = refBtnPrint;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if(Utils.isInternalPrinterSetting(ReprintCashInOutActivity.this)){
                WintecPrinter wtPrinter = new WintecPrinter(ReprintCashInOutActivity.this);
                wtPrinter.createTextForPrintCashInOutReceipt(transactionId, cashType, true);
                wtPrinter.print();
            }else{
                EPSONPrinter epPrinter = new EPSONPrinter(ReprintCashInOutActivity.this);
                epPrinter.createTextForPrintCashInOutReceipt(transactionId, cashType, true);
                epPrinter.print();
            }
            runOnUiThread(new Runnable(){

                @Override
                public void run() {
                    mBtnPrint.setEnabled(true);
                }

            });
            return null;
        }
    }
}

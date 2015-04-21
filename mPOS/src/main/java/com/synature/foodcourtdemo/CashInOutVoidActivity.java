package com.synature.foodcourtdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.synature.foodcourtdemo.datasource.CashInOutDao;
import com.synature.foodcourtdemo.datasource.CashInOutDataSource;
import com.synature.foodcourtdemo.datasource.model.CashInOutOrderTransaction;
import com.synature.foodcourtdemo.datasource.model.OrderTransaction;

import java.util.List;

/**
 * Created by j1tth4 on 18/3/2558.
 */
public class CashInOutVoidActivity extends Activity {

    private CashInOutDao mCashInOutDao;
    private List<CashInOutOrderTransaction> mTransLst;
    private CashInOutBillAdapter mCashInOutAdapter;

    private int mTransId = 0;
    private int mStaffId;

    private ListView mLvCashInOutBill;
    private CustomFontTextView mTvCashInOutBill;
    private MenuItem mItemConfirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_cash_inout_void);
        mLvCashInOutBill = (ListView) findViewById(R.id.lvCashInOutBill);
        mTvCashInOutBill = (CustomFontTextView) findViewById(R.id.tvCashInOutBill);
        mLvCashInOutBill.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OrderTransaction trans = (OrderTransaction) parent.getItemAtPosition(position);
                mTransId = trans.getTransactionId();
                showBillDetail(trans);
                enableItemConfirm(true);
            }
        });
        Intent intent = getIntent();
        mStaffId = intent.getIntExtra("staffId", 0);

        mCashInOutDao = new CashInOutDataSource(this);
        setupCashInOutBillAdapter();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_confirm){
            confirm();
            return true;
        }else if(id == android.R.id.home){
            finish();
            return true;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_cash_inout_void, menu);
        mItemConfirm = menu.findItem(R.id.action_confirm);
        enableItemConfirm(false);
        return true;
    }

    private void enableItemConfirm(boolean isEnable){
        if(mItemConfirm != null){
            mItemConfirm.setEnabled(isEnable);
        }
    }

    private void confirm(){
        View remarkView = getLayoutInflater().inflate(R.layout.input_text_layout, null, false);
        final EditText txt = (EditText) remarkView.findViewById(R.id.editText1);
        txt.setHint(getString(R.string.reason));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.void_cash_inout);
        builder.setView(remarkView);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setPositiveButton(android.R.string.ok, null);
        final AlertDialog cfDialog = builder.create();
        cfDialog.show();
        cfDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String remark = txt.getText().toString();
                if(!TextUtils.isEmpty(remark)){
                    mCashInOutDao.voidTransaction(mTransId, MPOSApplication.sComputerId,
                            mStaffId, remark);
                    setupCashInOutBillAdapter();
                    showBillDetail(null);
                    enableItemConfirm(false);
                    cfDialog.dismiss();
                }else{
                    txt.setError(getString(R.string.enter_reason));
                }
            }
        });
    }

    private void showBillDetail(OrderTransaction trans){
        if(trans != null) {
            mTvCashInOutBill.setText(trans.getEj());
        }else{
            mTvCashInOutBill.setText(null);
        }
    }

    private void loadTransaction(){
        mTransLst = mCashInOutDao.listCashInOutTransaction(Utils.getISODate());
    }

    private void setupCashInOutBillAdapter(){
        loadTransaction();
        if(mCashInOutAdapter == null){
            mCashInOutAdapter = new CashInOutBillAdapter();
            mLvCashInOutBill.setAdapter(mCashInOutAdapter);
        }else{
            mCashInOutAdapter.notifyDataSetChanged();
        }
    }

    private class CashInOutBillAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mTransLst != null ? mTransLst.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mTransLst.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.receipt_template, parent, false);
                holder.tvBill = (TextView) convertView.findViewById(R.id.tvReceiptNo);
                holder.tvCashOutTime = (TextView) convertView.findViewById(R.id.tvPaidTime);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            OrderTransaction trans = mTransLst.get(position);
            holder.tvBill.setText(trans.getReceiptNo());
            holder.tvCashOutTime.setText(Utils.dateTimeFormat(Utils.convertISODateTimeToCalendar(trans.getOpenTime())));
            return convertView;
        }

        class ViewHolder{
            TextView tvBill;
            TextView tvCashOutTime;
        }
    }
}

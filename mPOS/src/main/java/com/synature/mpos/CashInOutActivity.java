package com.synature.mpos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.synature.mpos.datasource.CashInOutDataSource;
import com.synature.mpos.datasource.GlobalPropertyDataSource;
import com.synature.mpos.datasource.SaleTransaction;
import com.synature.mpos.datasource.model.CashInOutOrderDetail;
import com.synature.pos.CashInOutProduct;
import com.synature.pos.GlobalProperty;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class CashInOutActivity extends Activity {

    private CashInOutDataSource mCashDataSource;
    private List<CashInOutProduct> mCashLst;
    private List<CashInOutOrderDetail> mCashDetailLst;

    private CashInOutDetailAdapter mCashDetailAdapter;
    private CashInOutProductAdapter mCashProductAdapter;

    private int mCashInOutTransId;
    private int mCashInOutCompId;
    private int mStaffId;
    private int mSessionId;
    private int mCashType;

    private DecimalFormat mDecFormat;

    private ListView mLvCashInout;
    private GridView mGvCashInout;
    private TextView mTvCashInOutTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_in_out);

        mLvCashInout = (ListView) findViewById(R.id.lvCashInOut);
        mGvCashInout = (GridView) findViewById(R.id.gvCashInOut);
        mTvCashInOutTotalPrice = (TextView) findViewById(R.id.tvCashInOutTotalPrice);

        Intent intent = getIntent();
        mCashInOutCompId = intent.getIntExtra("computerId", 0);
        mStaffId = intent.getIntExtra("staffId", 0);
        mSessionId = intent.getIntExtra("sessionId", 0);
        mCashType = intent.getIntExtra("cashType", -1);

        mCashDataSource = new CashInOutDataSource(this);
        GlobalPropertyDataSource global = new GlobalPropertyDataSource(this);
        mDecFormat = new DecimalFormat(global.getGlobalProperty().getCurrencyFormat());

        openCashInOutTransaction();
        setupCashProductAdapter();
        setupCashDetailAdapter();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cash_in_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_confirm) {
            return true;
        }else if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        cancelCashInOutTransaction();
        super.onDestroy();
    }

    private void setupCashDetailAdapter(){
        loadCashDetail();
        if(mCashDetailAdapter == null){
            mCashDetailAdapter = new CashInOutDetailAdapter();
            mLvCashInout.setAdapter(mCashDetailAdapter);
        }else {
            mCashDetailAdapter.notifyDataSetChanged();
        }
    }

    private void setupCashProductAdapter(){
        loadCashProduct();
        if(mCashProductAdapter == null){
            mCashProductAdapter = new CashInOutProductAdapter();
            mGvCashInout.setAdapter(mCashDetailAdapter);
        }else{
            mCashProductAdapter.notifyDataSetChanged();
        }
    }

    private void loadCashDetail(){
        mCashDetailLst = mCashDataSource.listAllCashInOutDetail(mCashInOutTransId);
    }

    private void loadCashProduct(){
        mCashLst = mCashDataSource.listAllCashInOutProduct();
    }

    private void cancelCashInOutTransaction(){
        mCashDataSource.cancelTransaction(mCashInOutTransId, mCashInOutCompId);
    }

    private void openCashInOutTransaction(){
        mCashDataSource.openTransaction(mCashInOutCompId, mStaffId, mSessionId, mCashType);
    }

    private class CashInOutDetailAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mCashDetailLst != null ? mCashDetailLst.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mCashDetailLst.get(position);
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
                convertView = getLayoutInflater().inflate(R.layout.list_item_cash_inout, parent, false);
                holder.tvItemName = (TextView) convertView.findViewById(R.id.tvItemName);
                holder.txtCashInOutAmount = (EditText) convertView.findViewById(R.id.txtCashInOutAmount);
                holder.btnDel = (ImageButton) convertView.findViewById(R.id.btnDel);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            CashInOutOrderDetail cashDetail = mCashDetailLst.get(position);
            holder.tvItemName.setText(cashDetail.getProductName());
            holder.txtCashInOutAmount.setText(mDecFormat.format(cashDetail.getfCashOutPrice()));
            holder.btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return convertView;
        }

        class ViewHolder{
            TextView tvItemName;
            EditText txtCashInOutAmount;
            ImageButton btnDel;
        }
    }

    private class CashInOutProductAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mCashLst != null ? mCashLst.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mCashLst.get(position);
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
                convertView = getLayoutInflater().inflate(R.layout.button_template, parent, false);
                holder.btn = (Button) convertView;
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            CashInOutProduct cash = mCashLst.get(position);
            holder.btn.setText(cash.getCashInOutName());
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return convertView;
        }

        class ViewHolder{
            Button btn;
        }
    }
}

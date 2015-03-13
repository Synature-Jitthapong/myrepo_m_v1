package com.synature.mpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.synature.mpos.datasource.CashInOutDataSource;
import com.synature.mpos.datasource.GlobalPropertyDataSource;
import com.synature.mpos.datasource.model.CashInOutOrderDetail;
import com.synature.pos.CashInOutProduct;

import java.text.DecimalFormat;
import java.util.List;


public class CashInOutActivity extends Activity {

    private CashInOutDataSource mCashDataSource;
    private List<CashInOutProduct> mCashLst;
    private List<CashInOutOrderDetail> mCashDetailLst;

    private CashInOutDetailAdapter mCashDetailAdapter;
    private CashInOutProductAdapter mCashProductAdapter;

    private double mTotalPrice;

    private int mCashInOutTransId;
    private int mCashInOutCompId;
    private int mStaffId;
    private int mSessionId;
    private int mCashType;
    private String mCashTypeName;

    private DecimalFormat mDecFormat;

    private ListView mLvCashInout;
    private GridView mGvCashInout;
    private TextView mTvCashInOutTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_in_out);

        //getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        mLvCashInout = (ListView) findViewById(R.id.lvCashInOut);
        mGvCashInout = (GridView) findViewById(R.id.gvCashInOut);
        mTvCashInOutTotalPrice = (TextView) findViewById(R.id.tvCashInOutTotalPrice);

        Intent intent = getIntent();
        mCashInOutCompId = MPOSApplication.sComputerId;
        mStaffId = intent.getIntExtra("staffId", 0);
        mSessionId = intent.getIntExtra("sessionId", 0);
        mCashType = intent.getIntExtra("cashType", -1);
        mCashTypeName = intent.getStringExtra("cashTypeName");

        mCashDataSource = new CashInOutDataSource(this);
        GlobalPropertyDataSource global = new GlobalPropertyDataSource(this);
        mDecFormat = new DecimalFormat(global.getGlobalProperty().getCurrencyFormat());

        openCashInOutTransaction();
        setupCashProductAdapter();
        setupCashDetailAdapter();
        setTitle(mCashTypeName);
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
            confirm();
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
            mGvCashInout.setAdapter(mCashProductAdapter);
        }else{
            mCashProductAdapter.notifyDataSetChanged();
        }
    }

    private void totalCashAmount(){
        mTotalPrice = mCashDataSource.getTotalCashAmount(mCashInOutTransId, mCashInOutCompId);
        mTvCashInOutTotalPrice.setText(mDecFormat.format(mTotalPrice));
    }

    private void loadCashDetail(){
        mCashDetailLst = mCashDataSource.listAllCashInOutDetail(mCashInOutTransId);
        totalCashAmount();
    }

    private void loadCashProduct(){
        mCashLst = mCashDataSource.listAllCashInOutProduct(mCashType);
    }

    private void confirm(){
        if(mCashDetailLst != null && mCashDetailLst.size() > 0) {
            View inputView = getLayoutInflater().inflate(R.layout.input_text_layout, null, false);
            final EditText txt = (EditText) inputView.findViewById(R.id.editText1);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.remark);
            builder.setView(inputView);
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton(android.R.string.ok, null);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String remark = txt.getText().toString();
                    mCashDataSource.closeTransaction(mCashInOutTransId, mCashInOutCompId,
                            mSessionId, mTotalPrice, remark);
                    openCashInOutTransaction();
                    setupCashDetailAdapter();
                    dialog.dismiss();
                }
            });
        }
    }

    private void cancelCashInOutTransaction(){
        mCashDataSource.cancelTransaction(mCashInOutTransId, mCashInOutCompId);
    }

    private void openCashInOutTransaction(){
        mCashInOutTransId = mCashDataSource.openTransaction(mCashInOutCompId, mStaffId, mSessionId, mCashType);
    }

    private void addCashInOut(final int cashInOutId, String cashInOutName, final int cashType){
        View inputView = getLayoutInflater().inflate(R.layout.input_text_layout, null, false);
        final EditText txt = (EditText) inputView.findViewById(R.id.editText1);
        txt.setHint(getString(R.string.enter_price));
        txt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(cashInOutName);
        builder.setView(inputView);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        builder.setPositiveButton(android.R.string.ok, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtPrice = txt.getText().toString();
                if (!TextUtils.isEmpty(txtPrice)) {
                    try {
                        double price = Double.parseDouble(txtPrice);
                        mCashDataSource.insertDetail(mCashInOutTransId, cashInOutId, price, cashType);
                        setupCashDetailAdapter();
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        txt.setError(getString(R.string.enter_valid_numeric));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    txt.setError(getString(R.string.enter_price));
                }
            }
        });
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
            final CashInOutOrderDetail cashDetail = mCashDetailLst.get(position);
            holder.tvItemName.setText(cashDetail.getProductName());
            holder.txtCashInOutAmount.setText(mDecFormat.format(cashDetail.getfCashOutPrice()));
            holder.btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(CashInOutActivity.this)
                            .setTitle(R.string.delete)
                            .setMessage(R.string.confirm_delete_item)
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mCashDataSource.deleteDetail(mCashInOutTransId, cashDetail.getiOrderID());
                                    setupCashDetailAdapter();
                                }
                            }).show();
                }
            });
            holder.txtCashInOutAmount.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if(event.getAction() != KeyEvent.ACTION_DOWN)
                        return true;

                    if(keyCode == KeyEvent.KEYCODE_ENTER){
                        EditText txt = (EditText) v;
                        if(!TextUtils.isEmpty(txt.getText().toString())) {
                            try {
                                double price = Double.parseDouble(txt.getText().toString());
                                int affectedRow = mCashDataSource.updateDetail(
                                        mCashInOutTransId, cashDetail.getiOrderID(), price);
                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                txt.setError(getString(R.string.enter_valid_numeric));
                            }
                        }else{
                            txt.setError(getString(R.string.enter_price));
                        }
                    }
                    return false;
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
                holder.btn.setMinHeight(96);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            final CashInOutProduct cash = mCashLst.get(position);
            holder.btn.setText(cash.getCashInOutName());
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCashInOut(cash.getCashInOutId(), cash.getCashInOutName(), cash.getCashInOutType());
                }
            });
            return convertView;
        }

        class ViewHolder{
            Button btn;
        }
    }
}

package com.synature.mpos;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.synature.mpos.datasource.SaleTransaction;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by j1tth4 on 2/23/15.
 */
public class RestoreSaleTransFragment extends DialogFragment implements
        LastSaleTransactionLoader.LoadSaleTransactionCallback{

    private List<SaleTransaction.SaleTable_OrderTransaction> mSaleTransLst;
    private SaleTransAdapter mSaleTransAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return super.onCreateDialog(savedInstanceState);
    }

    private void setupAdapter(){
        if(mSaleTransAdapter == null){
            mSaleTransLst = new ArrayList<SaleTransaction.SaleTable_OrderTransaction>();
            mSaleTransAdapter = new SaleTransAdapter();
        }else{
            mSaleTransAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadSuccess(LastSaleTransactionLoader.BackSaleTransaction saleTrans) {

    }

    @Override
    public void onLoadError(String msg) {

    }

    private class SaleTransAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(
                        android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder();
                holder.tv = (TextView) convertView;
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            SaleTransaction.SaleTable_OrderTransaction trans =
                    mSaleTransLst.get(position);
            holder.tv.setText(position + ". " + trans.getSzReceiptNo());
            return convertView;
        }

        class ViewHolder{
            TextView tv;
        }
    }
}

package com.synature.mpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.synature.mpos.datasource.table.CashInOutOrderTransTable;

/**
 * Created by j1tth4 on 3/11/15.
 */
public class CashInOutSelectionDialogFragment extends DialogFragment{

    public static final String TAG = CashInOutSelectionDialogFragment.class.getSimpleName();

    public static final int CASH_OUT_TYPE = -1;
    public static final int CASH_IN_TYPE = 1;
    public static final int REPRINT_CASH_INOUT = 2;
    public static final int VOID_CASH_INOUT = 3;

    private CashInOutSelectionListener mCallback;

    private String[] mCashTypes;
    private CashInOutSelectionAdapter mCashInOutSelectionAdapter;

    private GridView mGvCashInOutSelection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View contentView = getActivity().getLayoutInflater().inflate(
                R.layout.cash_inout_selection_fragment, null, false);
        mGvCashInOutSelection = (GridView) contentView.findViewById(R.id.gvCashInOutSelection);
        setupCashInOutSelectionAdapter();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.cash_inout);
        builder.setView(contentView);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof CashInOutSelectionListener){
            mCallback = (CashInOutSelectionListener) activity;
        }
    }

    private void setupCashInOutSelectionAdapter(){
        mCashTypes = getResources().getStringArray(R.array.cash_type_menu);
        if(mCashInOutSelectionAdapter == null){
            mCashInOutSelectionAdapter = new CashInOutSelectionAdapter();
            mGvCashInOutSelection.setAdapter(mCashInOutSelectionAdapter);
        }else{
            mCashInOutSelectionAdapter.notifyDataSetChanged();
        }
    }

    private class CashInOutSelectionAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mCashTypes.length;
        }

        @Override
        public Object getItem(int position) {
            return mCashTypes[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                holder = new ViewHolder();
                convertView = getActivity().getLayoutInflater().inflate(R.layout.button_template, parent, false);
                holder.btn = (Button) convertView;
                holder.btn.setMinHeight(96);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            final String cashType = mCashTypes[position];
            holder.btn.setText(cashType);
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(position == 0){
                        mCallback.onSelected(CASH_OUT_TYPE, cashType);
                    }else if(position == 1){
                        mCallback.onSelected(CASH_IN_TYPE, cashType);
                    }else if(position == 2){
                        mCallback.onSelected(REPRINT_CASH_INOUT, cashType);
                    }else if(position == 3){
                        mCallback.onSelected(VOID_CASH_INOUT, cashType);
                    }
                    getDialog().dismiss();
                }
            });
            return convertView;
        }

        class ViewHolder{
            Button btn;
        }
    }

    public static interface CashInOutSelectionListener{
        void onSelected(int type, String typeName);
    }
}

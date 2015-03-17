package com.synature.mpos;

import java.util.Calendar;

import com.synature.mpos.datasource.OrderTransDataSource;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ClearSaleDialogFragment extends DialogFragment implements OnClickListener{

	public static final String TAG = ClearSaleDialogFragment.class.getSimpleName();
	public static final String PASS = "mposclear";

	private String mDateFrom;
	private String mDateTo;
	
	public static ClearSaleDialogFragment newInstance(){
		ClearSaleDialogFragment f = new ClearSaleDialogFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        String date = Utils.dateFormat(calendar);
        String dateISO = Utils.getISODate();
		mDateFrom = dateISO;
		mDateTo = dateISO;
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		View content = inflater.inflate(R.layout.fragment_clear_sale, null);
		Button btnDateFrom = (Button) content.findViewById(R.id.btnDateFrom);
		Button btnDateTo = (Button) content.findViewById(R.id.btnDateTo);
		btnDateFrom.setText(date);
		btnDateTo.setText(date);
		btnDateFrom.setOnClickListener(this);
		btnDateTo.setOnClickListener(this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.clear_sale_data);
		builder.setView(content);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		builder.setPositiveButton(android.R.string.ok, null);
		final AlertDialog d = builder.create();
		d.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
                Calendar cFrom = Utils.convertISODateToCalendar(mDateFrom);
                Calendar cTo = Utils.convertISODateToCalendar(mDateTo);
				if(cTo.compareTo(cFrom) >= 0){
					String msg = getString(R.string.clear_sale_msg) + "\n"
							+ getString(R.string.from) + " "
							+ Utils.dateFormat(Utils.convertISODateToCalendar(mDateFrom))
							+ " " + getString(R.string.to) + " "
							+ Utils.dateFormat(Utils.convertISODateToCalendar(mDateTo));
					View passContent = inflater.inflate(R.layout.edittext_password, null);
					final EditText txtPass = (EditText) passContent.findViewById(R.id.txtPassword);
					txtPass.setHint(R.string.enter_password);
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(R.string.clear_sale_data);
					builder.setMessage(msg);
					builder.setView(passContent);
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					builder.setPositiveButton(android.R.string.ok, null);
					final AlertDialog df = builder.create();
					df.show();
					df.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							String pass = txtPass.getText().toString();
							if(!TextUtils.isEmpty(pass)){
								if(pass.equals(PASS)){
									clearSale();
									df.dismiss();
									d.dismiss();
								}else{
									txtPass.setError(getString(R.string.incorrect_password));
								}
							}else{
								txtPass.setError(getString(R.string.enter_password));
							}
						}
					});
				}else{
					createAlertDialog(getString(R.string.clear_sale_data), 
							"incorrect date!");
				}
			}
			
		});
		return d;
	}

	private void createAlertDialog(String title, String msg){
		new AlertDialog.Builder(getActivity())
		.setTitle(title)
		.setMessage(msg)
		.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
	}
	
	@Override
	public void onClick(final View v) {
		DialogFragment df;
		switch(v.getId()){
		case R.id.btnDateFrom:
			df = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
				
				@Override
				public void onSetDate(String dateISO) {
					mDateFrom = dateISO;
					((Button) v).setText(Utils.dateFormat(Utils.convertISODateToCalendar(dateISO)));
				}
			});
			df.show(getFragmentManager(), TAG);
			break;
		case R.id.btnDateTo:
			df = new DatePickerFragment(new DatePickerFragment.OnSetDateListener() {
				
				@Override
				public void onSetDate(String dateISO) {
					mDateTo = dateISO;
					((Button) v).setText(Utils.dateFormat(Utils.convertISODateToCalendar(dateISO)));
				}
			});
			df.show(getFragmentManager(), TAG);
			break;
		}
	}
	
	private void clearSale(){
		OrderTransDataSource trans = new OrderTransDataSource(getActivity());
		trans.deleteAllSale(mDateFrom, mDateTo);
		createAlertDialog(getString(R.string.clear_sale_data), "Clear sale data success.");
	}
}

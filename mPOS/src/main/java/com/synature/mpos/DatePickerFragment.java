package com.synature.mpos;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;

@SuppressLint("ValidFragment")
public class DatePickerFragment extends DialogFragment 
	implements DatePickerDialog.OnDateSetListener {

	private OnSetDateListener listener;
	
	public DatePickerFragment(OnSetDateListener onSetDateListener){
		listener = onSetDateListener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int month,
			int dayOfMonth) {
		listener.onSetDate(year + "-" + month + "-" + dayOfMonth);
	}

	public static interface OnSetDateListener{
		void onSetDate(String dateISO);
	}
}

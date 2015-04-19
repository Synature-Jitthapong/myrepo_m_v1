package com.synature.mpos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.synature.mpos.datasource.GlobalPropertyDataSource;
import com.synature.mpos.datasource.MPOSDatabase;
import com.synature.mpos.datasource.SessionDataSource;
import com.synature.mpos.datasource.model.Session;
import com.synature.mpos.datasource.table.BaseColumn;
import com.synature.mpos.datasource.table.SessionDetailTable;
import com.synature.mpos.datasource.table.SessionTable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class SendEnddayActivity extends ActionBarActivity {

    public static final int IGNORE_SEND_STATUS = 1;
	
	private int mStaffId;
	private int mShopId;
	private int mComputerId;
    private int mIgnoreSendStatus;

	private List<Session> mSessLst;
	private EnddayListAdapter mEnddayAdapter;
	
	private ListView mLvEndday;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	            WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	    LayoutParams params = getWindow().getAttributes();
	    params.width = 500;
	    params.height= 500;
	    params.alpha = 1.0f;
	    params.dimAmount = 0.5f;
	    getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    setFinishOnTouchOutside(false);
		setContentView(R.layout.activity_send_endday);
		
		mLvEndday = (ListView) findViewById(R.id.lvEndday);

		Intent intent = getIntent();
		mStaffId = intent.getIntExtra("staffId", 0);
		mShopId = MPOSApplication.sShopId;
		mComputerId = MPOSApplication.sComputerId;
        int ignoreSendStatus = intent.getIntExtra("ignoreSendStatus", 0);
        if(ignoreSendStatus == 1){
            mIgnoreSendStatus = ignoreSendStatus;
        }
		setupAdapter();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return false;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case android.R.id.home:
				setResult(RESULT_OK);
				finish();
			return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void setupAdapter(){
		mSessLst = listSessionEnddayNotSend();
        if(mEnddayAdapter == null){
            mEnddayAdapter = new EnddayListAdapter();
            mLvEndday.setAdapter(mEnddayAdapter);
        }
        mEnddayAdapter.notifyDataSetChanged();
	}
	
	private class EnddayReceiver extends ResultReceiver{

		private ProgressDialog progress;
		
		public EnddayReceiver(Handler handler) {
			super(handler);
			progress = new ProgressDialog(SendEnddayActivity.this);
			progress.setCanceledOnTouchOutside(false);
			progress.setMessage(getString(R.string.please_wait));
			progress.show();
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case EnddaySenderService.RESULT_SUCCESS:
				break;
			case EnddaySenderService.RESULT_ERROR:
				new AlertDialog.Builder(SendEnddayActivity.this)
				.setMessage(resultData.getString("msg"))
				.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				}).show();
				break;
			}
			progress.dismiss();
			setupAdapter();
		}
		
	}
	
	private class SendEnddayClickListener implements OnClickListener{

		private String sessionDate;
		
		public SendEnddayClickListener(String sessionDate){
			this.sessionDate = sessionDate;
		}
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(SendEnddayActivity.this, EnddaySenderService.class);
			intent.putExtra(EnddaySenderService.WHAT_TO_DO_PARAM, EnddaySenderService.SEND_CURRENT);
			intent.putExtra(EnddaySenderService.SESSION_DATE_PARAM, sessionDate);
			intent.putExtra(EnddaySenderService.SHOP_ID_PARAM, mShopId);
			intent.putExtra(EnddaySenderService.COMPUTER_ID_PARAM, mComputerId);
			intent.putExtra(EnddaySenderService.STAFF_ID_PARAM, mStaffId);
			intent.putExtra(EnddaySenderService.RECEIVER_NAME, new EnddayReceiver(new Handler()));
			startService(intent);
		}
		
	}
	
	private class EnddayListAdapter extends BaseAdapter{
		
		private LayoutInflater mInflater = getLayoutInflater();
		
		@Override
		public int getCount() {
			return mSessLst != null ? mSessLst.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return mSessLst.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.endday_list_template, parent, false);
				holder.tvSessionDate = (TextView) convertView.findViewById(R.id.tvSaleDate);
				holder.tvSummary = (TextView) convertView.findViewById(R.id.tvSummary);
				holder.btnSend = (ImageButton) convertView.findViewById(R.id.btnSendEndday);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			Session session = mSessLst.get(position); 
			String sessionDate = session.getSessionDate();
			holder.tvSessionDate.setText(Utils.dateFormat(Utils.convertISODateToCalendar(sessionDate)));
			holder.tvSummary.setText("#Bill " + Utils.qtyFormat(session.getTotalQtyReceipt())
					+ " Total " + Utils.currencyFormat(session.getTotalAmountReceipt()));
			holder.btnSend.setOnClickListener(new SendEnddayClickListener(sessionDate));
			return convertView;
		}
		
		class ViewHolder{
			TextView tvSessionDate;
			TextView tvSummary;
			ImageButton btnSend;
		}
	}

    public List<com.synature.mpos.datasource.model.Session> listSessionEnddayNotSend(){
        MPOSDatabase.MPOSOpenHelper helper = MPOSDatabase.MPOSOpenHelper.getInstance(this);
        SQLiteDatabase db = helper.getReadableDatabase();
		List<com.synature.mpos.datasource.model.Session> sessLst = null;
        String sql = "SELECT " + SessionTable.COLUMN_SESS_DATE + ","
                + SessionDetailTable.COLUMN_TOTAL_QTY_RECEIPT + ", "
                + SessionDetailTable.COLUMN_TOTAL_AMOUNT_RECEIPT
                + " FROM " + SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL
                + " WHERE " + SessionDetailTable.COLUMN_TOTAL_QTY_RECEIPT + " >? "
                + " AND " + BaseColumn.COLUMN_SEND_STATUS + "=?";
        String[] args = new String[]{
                String.valueOf(0),
                String.valueOf(MPOSDatabase.NOT_SEND)
        };
        if(mIgnoreSendStatus == 1){
            sql = "SELECT " + SessionTable.COLUMN_SESS_DATE + ","
                    + SessionDetailTable.COLUMN_TOTAL_QTY_RECEIPT + ", "
                    + SessionDetailTable.COLUMN_TOTAL_AMOUNT_RECEIPT
                    + " FROM " + SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL
                    + " WHERE " + SessionDetailTable.COLUMN_TOTAL_QTY_RECEIPT + " >? ";
            args = new String[]{
                    String.valueOf(0)
            };
        }
		Cursor cursor = db.rawQuery(sql, args);
		if(cursor.moveToFirst()){
			sessLst = new ArrayList<Session>();
			do{
				com.synature.mpos.datasource.model.Session session
					= new com.synature.mpos.datasource.model.Session();
				session.setSessionDate(cursor.getString(cursor.getColumnIndex(SessionTable.COLUMN_SESS_DATE)));
				session.setTotalQtyReceipt(cursor.getInt(cursor.getColumnIndex(SessionDetailTable.COLUMN_TOTAL_QTY_RECEIPT)));
				session.setTotalAmountReceipt(cursor.getDouble(cursor.getColumnIndex(SessionDetailTable.COLUMN_TOTAL_AMOUNT_RECEIPT)));
				sessLst.add(session);
			}while(cursor.moveToNext());
		}
		cursor.close();
		return sessLst;
	}
}

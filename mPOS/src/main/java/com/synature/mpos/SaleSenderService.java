package com.synature.mpos;

import com.synature.mpos.datasource.MPOSDatabase;
import com.synature.mpos.datasource.OrderTransDataSource;
import com.synature.util.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;

public class SaleSenderService extends SaleSenderServiceBase{

	public static final String TAG = SaleSenderService.class.getSimpleName();
	
	public static final int SEND_PARTIAL = 1;
	public static final int SEND_LAST_SALE_TRANS = 2;
	public static final int SEND_SPECIFIC_TRANS = 3;
	
	public static final String RECEIVER_NAME = "saleSenderReceiver";
	public static final String TRANS_ID_PARAM = "transactionId";

	private HandlerThread mSenderThread;
	private SaleSenderHandler mSaleSenderHandler;
	
	private final class SaleSenderHandler extends Handler{
		public SaleSenderHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			ResultReceiver receiver = (ResultReceiver) b.getParcelable(RECEIVER_NAME);
			String sessionDate = b.getString(SESSION_DATE_PARAM);
			int whatToDo = b.getInt(WHAT_TO_DO_PARAM, SEND_PARTIAL);
			int shopId = b.getInt(SHOP_ID_PARAM, 0);
			int computerId = b.getInt(COMPUTER_ID_PARAM, 0);
			int staffId = b.getInt(STAFF_ID_PARAM, 0);
			int transactionId = b.getInt(TRANS_ID_PARAM, 0);
			if(whatToDo == SEND_PARTIAL){
				sendPartialSale(sessionDate, shopId, computerId, staffId, receiver);
			}else if(whatToDo == SEND_LAST_SALE_TRANS){
				sendLastSaleTransaction(sessionDate, shopId, computerId, staffId);
			}else if(whatToDo == SEND_SPECIFIC_TRANS){
				sendSpecificSaleTransaction(transactionId, sessionDate, shopId, computerId, staffId, receiver);
			}
		}
		
	}

	@Override
	public void onCreate() {
		mSenderThread = new HandlerThread("SaleSenderThread");
		mSenderThread.start();

		mSaleSenderHandler = new SaleSenderHandler(mSenderThread.getLooper());
	}
	
	@Override
	public void onDestroy() {
		mSenderThread.quit();
		super.onDestroy();
		Log.d(TAG, "Sale Sender Service stop");
	}

	/**
	 * Intent request 
	 * ResultReceiver, 
	 * sessionDate, 
	 * shopId, 
	 * computerId, 
	 * staffId  
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Sale Sender Service start");
		if(intent.getExtras() != null){
			Message msg = new Message();
			msg.arg1 = startId;
			Bundle b = intent.getExtras();
			msg.setData(b);
			mSaleSenderHandler.sendMessage(msg);
		}else{
			stopSelf();
		}
		return START_NOT_STICKY;
	}

	/**
	 * @author j1tth4
	 * The receiver for send partial sale data
	 */
	private class SendSaleReceiver extends ResultReceiver{

		private ResultReceiver receiver;
		private String sessionDate;
		private String jsonSale;
		
		public SendSaleReceiver(Handler handler, String sessionDate, 
				String jsonSale, ResultReceiver receiver) {
			super(handler);
			this.sessionDate = sessionDate;
			this.jsonSale = jsonSale;
			this.receiver = receiver;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case RESULT_SUCCESS:
				flagSendStatus(sessionDate, MPOSDatabase.ALREADY_SEND);
//				if(!TextUtils.isEmpty(jsonSale)){
//					try {
//						JSONSaleLogFile.appendSale(getApplicationContext(), jsonSale);
//					} catch (Exception e) {}
//					Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME,
//							"Send partial successfully");
//				}
				if(receiver != null){
					receiver.send(RESULT_SUCCESS, null);
				}
				stopSelf();
				break;
			case RESULT_ERROR:
				flagSendStatus(sessionDate, MPOSDatabase.NOT_SEND);
				String msg = resultData.getString("msg");
				Logger.appendLog(getApplicationContext(), MPOSApplication.ERR_LOG_PATH, "", 
						"Send partial fail: " + msg);
				if(receiver != null){
					Bundle b = new Bundle();
					b.putString("msg", msg);
					receiver.send(RESULT_ERROR, b);
				}
				stopSelf();
				break;
			}
		}
		
	}
	
	/**
	 * @author j1tth4
	 * The receiver for send specific transaction
	 */
	private class SendSpecificReceiver extends ResultReceiver{

		private ResultReceiver receiver;
		private int transactionId;
		
		public SendSpecificReceiver(Handler handler, int transactionId, ResultReceiver receiver) {
			super(handler);
			this.transactionId = transactionId;
			this.receiver = receiver;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			super.onReceiveResult(resultCode, resultData);
			switch(resultCode){
			case RESULT_SUCCESS:
				flagSendStatus(transactionId, MPOSDatabase.ALREADY_SEND);
				if(receiver != null){
					receiver.send(RESULT_SUCCESS, null);
				}
				stopSelf();
				break;
			case RESULT_ERROR:
				flagSendStatus(transactionId, MPOSDatabase.NOT_SEND);
				String msg = resultData.getString("msg");
				Logger.appendLog(getApplicationContext(), MPOSApplication.ERR_LOG_PATH, "", 
						"Send partial fail: " + msg);
				if(receiver != null){
					Bundle b = new Bundle();
					b.putString("msg", msg);
					receiver.send(RESULT_ERROR, b);
				}
				stopSelf();
				break;
			}
		}
		
	}
	
	/**
	 * Send partial sale data
	 * @param sessionDate
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param receiver
	 */
	private synchronized void sendPartialSale(String sessionDate, int shopId, int computerId,
			int staffId, ResultReceiver receiver){
		JSONSaleSerialization jsonGenerator = 
				new JSONSaleSerialization(getApplicationContext());
		String json = jsonGenerator.generateSale(sessionDate);

//		Log.d("SendPartial ", json);
//		Logger.appendLog(getApplicationContext(), MPOSApplication.LOG_PATH, "json", json);

		PartialSaleSender sender = new PartialSaleSender(getApplicationContext(), shopId, computerId,staffId, json, 
				new SendSaleReceiver(new Handler(), sessionDate, json, receiver));
		sender.run();
	}
	
	/**
	 * @param sessionDate
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 */
	private synchronized void sendLastSaleTransaction(String sessionDate, int shopId,
			int computerId, int staffId){
		JSONSaleSerialization jsonGenerator = 
				new JSONSaleSerialization(getApplicationContext());
		String jsonSale = jsonGenerator.generateLastSaleTransaction(sessionDate);
		PartialSaleSender sender = new PartialSaleSender(getApplicationContext(), shopId, computerId, staffId, jsonSale, 
				new SendSaleReceiver(new Handler(), sessionDate, jsonSale, null));
		sender.run();
	}
	
	/**
	 * @param transactionId
	 * @param sessionDate
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param receiver
	 */
	private synchronized void sendSpecificSaleTransaction(int transactionId, String sessionDate, int shopId,
			int computerId, int staffId, ResultReceiver receiver){
		JSONSaleSerialization jsonGenerator = 
				new JSONSaleSerialization(getApplicationContext());
		String jsonSale = jsonGenerator.generateSpecificSaleTransaction(transactionId, sessionDate);
		PartialSaleSender sender = new PartialSaleSender(getApplicationContext(), shopId, computerId, staffId, jsonSale, 
				new SendSpecificReceiver(new Handler(), transactionId, receiver));
		sender.run();
	}
	
	/**
	 * @param sessionDate
	 * @param status
	 */
	private void flagSendStatus(String sessionDate, int status){
		OrderTransDataSource trans = new OrderTransDataSource(getApplicationContext());
		trans.updateTransactionSendStatus(sessionDate, status);
		trans.updateTransactionWasteSendStatus(sessionDate, status);
	}
	
	/**
	 * @param transactionId
	 * @param status
	 */
	private void flagSendStatus(int transactionId, int status){
		OrderTransDataSource trans = new OrderTransDataSource(getApplicationContext());
		trans.updateTransactionSendStatus(transactionId, status);
		trans.updateTransactionWasteSendStatus(transactionId, status);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}

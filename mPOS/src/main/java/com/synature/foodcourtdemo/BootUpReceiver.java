package com.synature.foodcourtdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver{
	public static final String TAG = BootUpReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(loginIntent);
	}
}
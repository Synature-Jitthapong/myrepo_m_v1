package com.synature.mpos;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class InstallerReceiver extends BroadcastReceiver{

	public static final String TAG = InstallerReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

		String apkFileName = sharedPref.getString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_FILE_NAME, "");
		if(!TextUtils.isEmpty(apkFileName)){
			File sdPath = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File apk = new File(sdPath + File.separator + apkFileName);
			if(apk.exists()){
				apk.delete();
			}
		}

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(SettingsActivity.KEY_PREF_SYNC_TIME, "");
		editor.putBoolean(SettingsActivity.KEY_PREF_IS_SYNC, false);
		editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_FILE_NAME, "");
		editor.putString(SettingsActivity.KEY_PREF_APK_DOWNLOAD_STATUS, "0");
		editor.putString(SettingsActivity.KEY_PREF_NEED_TO_UPDATE, "0");
		editor.putString(SettingsActivity.KEY_PREF_NEW_VERSION, "");
		editor.putString(SettingsActivity.KEY_PREF_FILE_URL, "");
		editor.putString(SettingsActivity.KEY_PREF_SYNC_TIME, "");
		editor.commit();

//		Intent loginIntent = new Intent(context, LoginActivity.class);
//		loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(loginIntent);
	}
}

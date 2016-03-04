package com.synature.mpos;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.synature.mpos.datasource.ComputerDataSource;
import com.synature.mpos.datasource.SessionDataSource;
import com.synature.mpos.datasource.ShopDataSource;
import com.synature.util.Logger;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

public class MPOSApplication extends Application {
	/**
	 * Database name
	 */
	public static final String DB_NAME = "mpos.db";
	
	/**
	 * Database version
	 */
	public static int DB_VERSION = 10;
	
	/**
	 * Webservice url for check registered device.
	 */
	public static final String REGISTER_URL = "http://www.promise-system.com/promise_registerpos/ws_mpos.asmx";
	
	/**
	 * Url to post uncaught exception
	 */
	public static final String STACK_TRACE_URL = "http://www.promise-system.com/mpos_stack_trace/post_stack.php";
	
	/**
	 * WebService name
	 */
	public static final String WS_NAME = "ws_mpos.asmx";

	/**
	 * Menu image dir
	 */
	public static final String IMG_DIR = "mPOSImg";
	
	/**
	 * Prefix log file name
	 */
	public static final String LOG_FILE_NAME = "log_";
	
	/**
	 * Root dir
	 */
	public static final String RESOURCE_DIR = "mpos";
	
	/**
	 * Backup path
	 */
	public static final String BACKUP_DB_PATH = RESOURCE_DIR + File.separator + "backup";
	
	/**
	 * Log path
	 */
	public static final String LOG_PATH = RESOURCE_DIR + File.separator + "log";


	/**
	 * Error path
	 */
	public static final String ERR_LOG_PATH = RESOURCE_DIR + File.separator + "error";
	
	/**
	 * Path to store partial sale json file
	 */
	public static final String SALE_PATH = RESOURCE_DIR + File.separator + "Sale";
	
	/**
	 * Path to store endday sale json file
	 */
	public static final String ENDDAY_PATH = RESOURCE_DIR + File.separator + "EnddaySale";
	
	/**
	 * Image path on server
	 */
	public static final String SERVER_IMG_PATH = "Resources/Shop/MenuImage/";
	
	/**
	 * The minimum date
	 */
	public static final int MINIMUM_YEAR = 1900;
	public static final int MINIMUM_MONTH = 0;
	public static final int MINIMUM_DAY = 1;
	
	/**
	 * Enable/Disable log
	 */
	public static final boolean sIsEnableLog = true;

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
	}
}

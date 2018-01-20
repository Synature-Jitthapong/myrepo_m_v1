package com.synature.mpos;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.signature.StringSignature;
import com.epson.eposprint.EposException;
import com.synature.mpos.datasource.GlobalPropertyDataSource;
import com.synature.mpos.datasource.OrderTransDataSource;
import com.synature.mpos.datasource.ProductsDataSource;
import com.synature.mpos.datasource.SessionDataSource;
import com.synature.mpos.datasource.ShopDataSource;
import com.synature.pos.ShopProperty;
import com.synature.util.Logger;

@SuppressLint("ShowToast")
public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

	/**
	 * @param context
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param lastSessCal
	 * @return true if success endday
	 */
	public static boolean endingMultipleDay(Context context, int shopId, 
			int computerId, int staffId, Calendar lastSessCal){
		int diffDay = getDiffDay(lastSessCal);
		try {
			SessionDataSource sess = new SessionDataSource(context);
			// if have some previous session does not end
			String prevSessDate = String.valueOf(lastSessCal.getTimeInMillis());
			if(!sess.checkEndday(prevSessDate)){
				OrderTransDataSource trans = new OrderTransDataSource(context);
				int prevSessId = sess.getLastSessionId();
				sess.addSessionEnddayDetail(prevSessDate, 
						trans.getTotalReceipt(0, prevSessDate), 
						trans.getTotalReceiptAmount(prevSessDate));
				sess.closeSession(prevSessId, staffId, 0, true);
			}
			Calendar sessCal = (Calendar) lastSessCal.clone();
			for(int i = 1; i < diffDay; i++){
				sessCal.add(Calendar.DAY_OF_MONTH, 1);
				int sessId = sess.openSession(String.valueOf(sessCal.getTimeInMillis()), shopId, computerId, staffId, 0);
				sess.addSessionEnddayDetail(String.valueOf(sessCal.getTimeInMillis()), 0, 0);
				sess.closeSession(sessId, staffId, 0, true);
			}
			try {
				GlobalPropertyDataSource format = new GlobalPropertyDataSource(context);
				Logger.appendLog(context, MPOSApplication.LOG_PATH,
						MPOSApplication.LOG_FILE_NAME,
						"Success ending multiple day : " 
						+ " from : " + format.dateFormat(lastSessCal.getTime())
						+ " to : " + format.dateFormat(Calendar.getInstance().getTime()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.appendLog(context, MPOSApplication.LOG_PATH,
					MPOSApplication.LOG_FILE_NAME,
					"Error ending multiple day : " + e.getMessage());
		}
		return false;
	}
	
	/**
	 * @param lastSessCal
	 * @return get day number
	 */
	public static int getDiffDay(Calendar lastSessCal){
		Calendar currCalendar = Calendar.getInstance();
		int diffDay = 0;
		if(lastSessCal.get(Calendar.YEAR) == currCalendar.get(Calendar.YEAR)){
			diffDay = currCalendar.get(Calendar.DAY_OF_YEAR) - lastSessCal.get(Calendar.DAY_OF_YEAR);
		}else if(lastSessCal.get(Calendar.YEAR) < currCalendar.get(Calendar.YEAR)){
			diffDay = (lastSessCal.getActualMaximum(Calendar.DAY_OF_YEAR) 
					- lastSessCal.get(Calendar.DAY_OF_YEAR)) + currCalendar.get(Calendar.DAY_OF_YEAR);
		}	
		return diffDay;
	}
	
	public static Calendar getCalendar(){
		return Calendar.getInstance();
	}
	
	public static Calendar getMinimum(){
		return new GregorianCalendar(MPOSApplication.MINIMUM_YEAR, 
				MPOSApplication.MINIMUM_MONTH, MPOSApplication.MINIMUM_DAY);
	}
	
	public static Calendar getDate(int year, int month, int day){
		return new GregorianCalendar(year, month, day);
	}
	
	public static Calendar getDate(){
		Calendar c = getCalendar();
		return new GregorianCalendar(c.get(Calendar.YEAR), 
				c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
	}

	public static Calendar getCloseTime(Context context){
		ShopDataSource shop = new ShopDataSource(context);
		SessionDataSource session = new SessionDataSource(context);
		ShopProperty shopProp = shop.getShopProperty();
		Calendar closeTime = Calendar.getInstance();
		closeTime.setTimeInMillis(Long.parseLong(session.getLastSessionDate()));
		try {
			Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(shopProp.getCloseHour());
			Calendar temp = Calendar.getInstance();
			temp.setTime(d);
			if(temp.get(Calendar.HOUR_OF_DAY) > 0)
				closeTime.add(Calendar.DAY_OF_MONTH, 1);
			closeTime.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
			closeTime.set(Calendar.HOUR, temp.get(Calendar.HOUR));
			closeTime.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
			closeTime.set(Calendar.SECOND, temp.get(Calendar.SECOND));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return closeTime;
	}

	public static Calendar convertStringToCalendar(String dateTime){
		Calendar calendar = getCalendar();
		if(dateTime == null || dateTime.isEmpty()){
			dateTime = String.valueOf(getMinimum().getTimeInMillis());
		}
		calendar.setTimeInMillis(Long.parseLong(dateTime));
		return calendar;
	}

	public static double getSaleBeforeVat(double saleIncVat, double vatRate, int vatType){
		double saleBeforeVat = saleIncVat;
		if(vatType == ProductsDataSource.VAT_TYPE_INCLUDED)
			saleBeforeVat = saleIncVat * 100 / (100 + vatRate);
		return saleBeforeVat;
	}

	public static double calculateVatAmount(double totalPrice, double vatRate, int vatType){
		double vatAmount = 0;
		if(vatType == ProductsDataSource.VAT_TYPE_INCLUDED)
			vatAmount = totalPrice * vatRate / (100 + vatRate);
		else if(vatType == ProductsDataSource.VAT_TYPE_EXCLUDE)
			vatAmount = totalPrice * vatRate / 100;
		BigDecimal bd = new BigDecimal(vatAmount);
		bd = bd.setScale(4, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	/**
	 * @param scale
	 * @param value
	 * @return string fixes digit
	 */
	public static String fixesDigitLength(GlobalPropertyDataSource format, int scale, double value){
		return format.currencyFormat(value, "#,##0.0000");
	}
	
	/**
	 * @param roundType
	 * @param price
	 * @return rounding value
	 */
	public static double roundingPrice(int roundType, double price){
		double result = price;
		long iPart;		// integer part
		double fPart;	// fractional part
		iPart = (long) price;
		fPart = price - iPart;
		switch(roundType){
		case 1:
		case 7:
			if(fPart > 0){
				iPart += 1;
				fPart = 0;
			}
			break;
		case 2:
			if(fPart < 0.5){
				fPart = 0;
			}else if (fPart == 0.5){
				fPart = 0.5;
			}else{
				iPart += 1;
				fPart = 0;
			}
			break;
		case 3:
			if(fPart > 0 && fPart < 0.25){
				fPart = 0.25;
			}else if(fPart > 0.25 && fPart <= 0.5){
				fPart = 0.5;
			}else if(fPart > 0.5){
				iPart += 1;
				fPart = 0;
			}
			break;
		case 4:
			fPart = 0;
			break;
		case 5:
			if(fPart < 0.5){
				fPart = 0;
			}else if (fPart >= 0.5){
				fPart = 0.5;
			}
			break;
		case 6:
			if(fPart < 0.25){
				fPart = 0;
			}else if(fPart >= 0.25 && fPart < 0.5){
				fPart = 0.25;
			}else if(fPart >= 0.5){
				fPart = 0.5;
			}	
			break;
		case 8:
			if(fPart < 0.5){
				fPart = 0;
			}else if (fPart >= 0.5){
				iPart += 1;
				fPart = 0;
			}
			break;
		case 9:
			if(fPart < 0.25){
				fPart = 0;
			}else if(fPart >= 0.25 && fPart < 0.5){
				fPart = 0.25;
			}else if(fPart >= 0.5){
				iPart += 1;
				fPart = 0;
			}	
			break;
		}
		result = iPart + fPart;
		return result;
	}

	public static double stringToDouble(String text){
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		Number num = null;
		try {
			num = format.parse(text);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return num != null ? num.doubleValue() : 0;
	}

	public static void logServerResponse(Context context, String msg){
		Logger.appendLog(context, MPOSApplication.LOG_PATH,
				MPOSApplication.LOG_FILE_NAME,
				" Server Response : " + msg);
	}
	
	/**
	 * Get software version
	 * @param context
	 * @return version name
	 */
	public static String getSoftWareVersion(Context context){
		String ver = "";
		PackageInfo pInfo;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			ver = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ver;
	}
	
	/**
	 * @param context
	 * @return android device id
	 */
	public static String getDeviceCode(Context context) {
		return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}
	
	/**
	 * @param context
	 * @return drawer baud rate
	 */
	public static String getWintecDrwBaudRate(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DRW_BAUD_RATE, "BAUD_38400");
	}
	
	/**
	 * @param context
	 * @return drawer dev path
	 */
	public static String getWintecDrwDevPath(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DRW_DEV_PATH, "/dev/ttySAC1");
	}
	
	/**
	 * @param context
	 * @return magnetic reader baud rate
	 */
	public static String getWintecMsrBaudRate(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_MSR_BAUD_RATE, "BAUD_9600");
	}
	
	/**
	 * @param context
	 * @return magnetic reader dev path
	 */
	public static String getWintecMsrDevPath(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_MSR_DEV_PATH, "/dev/ttySAC3");
	}
	
	/**
	 * @param context
	 * @return true if enable internal printer
	 */
	public static boolean isInternalPrinterSetting(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_PRINTER_INTERNAL, true);
	}
	
	/**
	 * @param context
	 * @return printer baud rate
	 */
	public static String getWintecPrinterBaudRate(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_BAUD_RATE, "BAUD_38400");
	}
	
	/**
	 * @param context
	 * @return printer dev path
	 */
	public static String getWintecPrinterDevPath(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_DEV_PATH, "/dev/ttySAC1");
	}
	
	/**
	 * @param context
	 * @return epson model name
	 */
	public static String getEPSONModelName(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_LIST, "");
	}

	public static boolean isEnableBluetoothPrinter(Context context){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_BLUETOOTH_PRINTER, false);
	}

	public static String getBluetoothAddress(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_BT_PRINTER_MAC_ADDRESS, "");
	}

	public static String getEposExceptionText(int state){
		switch(state){
			case EposException.SUCCESS:
				return "SUCCESS";
			case    EposException.ERR_PARAM:
				return "ERR_PARAM";
			case    EposException.ERR_OPEN:
				return "ERR_OPEN";
			case    EposException.ERR_CONNECT:
				return "ERR_CONNECT";
			case    EposException.ERR_TIMEOUT:
				return "ERR_TIMEOUT";
			case    EposException.ERR_MEMORY:
				return "ERR_MEMORY";
			case    EposException.ERR_ILLEGAL:
				return "ERR_ILLEGAL";
			case    EposException.ERR_PROCESSING:
				return "ERR_PROCESSING";
			case    EposException.ERR_UNSUPPORTED:
				return "ERR_UNSUPPORTED";
			case    EposException.ERR_OFF_LINE:
				return "ERR_OFF_LINE";
			case    EposException.ERR_FAILURE:
				return "ERR_FAILURE";
			default:
				return String.format("%d", state);
		}
	}

	/**
	 * @param context
	 * @return epson printer ip
	 */
	public static String getPrinterIp(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_PRINTER_IP, "");
	}
	
	/**
	 * @param context
	 * @return true if enable wintec customer display
	 */
	public static boolean isEnableWintecCustomerDisplay(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_ENABLE_DSP, true);
	}
	
	/**
	 * @param context
	 * @return dsp baud rate
	 */
	public static String getWintecDspBaudRate(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DSP_BAUD_RATE, "BAUD_9600");
	}
	
	/**
	 * @param context
	 * @return text line 2
	 */
	public static String getWintecDspTextLine2(Context context){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DSP_TEXT_LINE2, "mPOS");
	}
	
	/**
	 * @param context
	 * @return text line 1
	 */
	public static String getWintecDspTextLine1(Context context){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DSP_TEXT_LINE1, "Welcome to");
	}
	
	/**
	 * @param context
	 * @return dsp dev path
	 */
	public static String getWintecDspPath(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_DSP_DEV_PATH, "/dev/ttySAC3");
	}
	
	/**
	 * @param context
	 * @return second display ip
	 */
	public static String getSecondDisplayIp(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_SECOND_DISPLAY_IP, "");
	}
	
	/**
	 * @param context
	 * @return second display port
	 */
	public static int getSecondDisplayPort(Context context) {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int port = Integer.parseInt(context.getString(R.string.default_second_display_port));
		try {
			String prefPort = sharedPref.getString(SettingsActivity.KEY_PREF_SECOND_DISPLAY_PORT, "");
			if(!prefPort.equals("")){
				port = Integer.parseInt(prefPort);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return port;
	}
	
	/**
	 * @param context
	 * @return month number to keep sale default -60
	 */
	public static int getLastDayToClearSale(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		int days = -90;
		try {
			String strDay = sharedPref.getString(SettingsActivity.KEY_PREF_MONTHS_TO_KEEP_SALE, "-90");
			days = Integer.parseInt(strDay);
		} catch (Exception e) {}
		return days;
	}
	
	/**
	 * @param context
	 * @return true if enabled
	 */
	public static boolean isEnableBackupDatabase(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_ENABLE_BACKUP_DB, true);
	}
	
	/**
	 * @param context
	 * @return true if enable second display
	 */
	public static boolean isEnableSecondDisplay(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_ENABLE_SECOND_DISPLAY, false);
	}

	/**
	 * @param context
	 * @return menu image url
	 */
	public static String getImageUrl(Context context) {
		return getUrl(context) + "/" + MPOSApplication.SERVER_IMG_PATH;
	}

	/**
	 * @param context
	 * @return full webservice url
	 */
	public static String getFullUrl(Context context) {
		return getUrl(context) + "/" + MPOSApplication.WS_NAME;
	}

	/**
	 * @param context
	 * @return connection time out millisecond
	 */
	public static int getConnectionTimeOut(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		String strTimeOut = sharedPref.getString(SettingsActivity.KEY_PREF_CONN_TIME_OUT_LIST, "30");
		int timeOut = Integer.parseInt(strTimeOut);
		return timeOut * 1000;
	}
	
	public static String getUrl(Context context) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String url = sharedPref.getString(SettingsActivity.KEY_PREF_SERVER_URL, "");
		return checkProtocal(url);
	}
	
	public static String checkProtocal(String url){
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			// not found protocal
			url = "http://" + url;
		}
		return url;
	}
	
	/**
	 * @param context
	 * @return language code en_US, th_TH
	 */
	public static String getLangCode(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getString(SettingsActivity.KEY_PREF_LANGUAGE_LIST, "en_US");
	}
	
	/**
	 * @param context
	 * @return true if enable show menu image
	 */
	public static boolean isShowMenuImage(Context context){
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPref.getBoolean(SettingsActivity.KEY_PREF_SHOW_MENU_IMG, true);
	}
	
	/**
	 * Switch language
	 * @param context
	 * @param langCode
	 */
	public static void switchLanguage(Context context, String langCode){
//		Locale locale = new Locale(langCode);
//		Locale.setDefault(locale);
//		Configuration config = new Configuration();
//		config.locale = locale;
//		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

		Resources res = context.getResources();
		// Change locale settings in the app.
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		String[] langCodes = langCode.split("_");
		String language = langCodes[0];
		String country = langCodes[1];
		conf.locale = new Locale(language, country);
		res.updateConfiguration(conf, dm);
	}
	
	public static Locale getLocale(Context context){
		return new Locale(getLangCode(context));
	}

    /**
     * @param context
     * @return sync date time in millisec
     */
    public static String getSyncDateTime(Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_TIME, "");
    }

	/**
	 * Send sale data real time when close bill
	 * @param context
	 * @return
	 */
	public static boolean isSendSaleRealTimeWhenCloseBill(Context context){
		boolean isSendRealTime = true;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		isSendRealTime = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SEND_SALE_REAL_TIME, true);
		return isSendRealTime;
	}

	/**
	 * @param context
	 * @return true or false
	 */
	public static boolean isSendEnddayAutomaticallyWhenAppStart(Context context){
		boolean isSend = false;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		isSend = sharedPref.getBoolean(SettingsActivity.KEY_PREF_AUTO_SEND_ENDDAY, false);
		return isSend;
	}

    /**
     * @param context
     * @return true if already sync
     */
    public static boolean isAlreadySync(Context context){
        boolean isSync = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String syncTime = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_TIME, "");
        if(!TextUtils.isEmpty(syncTime)){
            Calendar current = Calendar.getInstance();
            Calendar lastSync = Calendar.getInstance();
            try {
                lastSync.setTimeInMillis(Long.parseLong(syncTime));
                if(current.get(Calendar.DAY_OF_YEAR) > lastSync.get(Calendar.DAY_OF_YEAR)){
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(SettingsActivity.KEY_PREF_IS_SYNC, false);
                    editor.commit();
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        isSync = sharedPref.getBoolean(SettingsActivity.KEY_PREF_IS_SYNC, false);
        return isSync;
    }

	public static void shutdown(){
		Process chperm;
		try {
			chperm = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(chperm.getOutputStream());

			os.writeBytes("shutdown\n");
			os.flush();

			chperm.waitFor();

		} catch (IOException e) {
			Log.d("Shutdown", e.getMessage());
		} catch (InterruptedException e) {
			Log.d("Shutdown", e.getMessage());
		}
	}
	
	public static void backupDatabase(Context context){
		String backupDbName = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) + "_" + MPOSApplication.DB_NAME;
		File sd = Environment.getExternalStorageDirectory();
		FileChannel source = null;
		FileChannel destination = null;
		File dbPath = context.getDatabasePath(MPOSApplication.DB_NAME);
		File sdPath = new File(sd, MPOSApplication.BACKUP_DB_PATH);
		if(!sdPath.exists())
			sdPath.mkdirs();
		deleteBackupDatabase(context);
		try {
			source = new FileInputStream(dbPath).getChannel();
			destination = new FileOutputStream(sdPath + File.separator + backupDbName).getChannel();
			destination.transferFrom(source, 0, source.size());
			source.close();
			destination.close();
			Toast.makeText(context, context.getString(R.string.backup_db_success), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public static void deleteBackupDatabase(Context context){
		File sd = Environment.getExternalStorageDirectory();
		File sdPath = new File(sd, MPOSApplication.BACKUP_DB_PATH);
		File files[] = sdPath.listFiles();
		if(files != null){
			Calendar deleteDate = Calendar.getInstance();
			deleteDate.add(Calendar.DAY_OF_YEAR, getLastDayToClearSale(context));
			for(File file : files){
				String fileName = file.getName().replace("_mpos.db", "");
				try {
					Date date = new SimpleDateFormat("yyyy-MM-dd").parse(fileName);
					Calendar fileDate = Calendar.getInstance();
					fileDate.setTime(date);

					if(fileDate.compareTo(deleteDate) <= 0){
						file.delete();
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Clear sale data from
	 * first day sale to (today - config)
	 * @param context
	 */
	public static void deleteOverSale(Context context){
		SessionDataSource sessionDao = new SessionDataSource(context);
		String firstDate = sessionDao.getFirstSessionDate();
		String lastDate = sessionDao.getLastEndDaySessionDate();
		if(!TextUtils.isEmpty(firstDate) && !TextUtils.isEmpty(lastDate)){
			int dayToKeep = getLastDayToClearSale(context);
			Calendar cFirst = Calendar.getInstance();
			Calendar cLast = Calendar.getInstance();
			cFirst.setTimeInMillis(Long.parseLong(firstDate));
			cLast.setTimeInMillis(Long.parseLong(lastDate));
			cLast.add(Calendar.DAY_OF_YEAR, dayToKeep);
			if(cLast.compareTo(cFirst) > 0){
				Log.i(TAG, DateFormat.getTimeInstance().format(Calendar.getInstance().getTime())
						+ " begin clear over sale");
				OrderTransDataSource trans = new OrderTransDataSource(context);
				trans.deleteSale(firstDate, String.valueOf(cLast.getTimeInMillis()));

				DateFormat format = DateFormat.getDateInstance(DateFormat.LONG);
				Logger.appendLog(context, MPOSApplication.LOG_PATH, MPOSApplication.LOG_FILE_NAME,
						"Clear sale from: " + format.format(cFirst.getTime()) + "\n"
								+ " to: " + format.format(cLast.getTime()));
			}
		}
	}
	
//	public static void resetSendDataStatus(Context context){
//		MPOSDatabase db = new MPOSDatabase(context);
//		ContentValues cv = new ContentValues();
//		cv.put(MPOSDatabase.COLUMN_SEND_STATUS, 0);
//		db.getWritableDatabase().update(SessionDetailTable.TABLE_SESSION_ENDDAY_DETAIL, cv, null, null);
//		db.getWritableDatabase().update(OrderTransTable.TABLE_ORDER_TRANS, cv, null, null);
//		Toast.makeText(context, "Reset successfully.", Toast.LENGTH_SHORT).show();
//	}
	
	/**
	 * list all file in specified path
	 * @param parentDir
	 * @return List<File>
	 */
	public static List<File> listFiles(File parentDir){
		List<File> inFiles = null;
		File[] files = parentDir.listFiles();
		if(files != null){
			inFiles = new ArrayList<File>();
			Arrays.sort(files);
			for(File file : files){
				if(file.isDirectory()){
					inFiles.addAll(listFiles(file));
				}else if(file.getName().endsWith(".db")){
					inFiles.add(file);
				}
			}
		}
		return inFiles;
	}

	public static StringSignature getGlideSignatureString(){
		return new StringSignature(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime()));
	}

	public static LinearLayout.LayoutParams getLinHorParams(float weight){
		return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
	}
}

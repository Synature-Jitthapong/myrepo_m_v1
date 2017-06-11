package com.synature.mpos;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.epson.eposprint.EposException;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

    public static final String KEY_PREF_IS_SYNC = "is_sync";
    public static final String KEY_PREF_SYNC_TIME = "sync_time";
	public static final String KEY_PREF_SERVER_URL = "server_url";
	public static final String KEY_PREF_CONN_TIME_OUT_LIST = "connection_time_out";
	
	public static final String KEY_PREF_PRINTER_IP = "printer_ip";
	public static final String KEY_PREF_PRINTER_LIST = "printer_list";
	public static final String KEY_PREF_PRINTER_INTERNAL = "printer_internal";
	public static final String KEY_PREF_PRINTER_DEV_PATH = "printer_wintec_dev_path";
	public static final String KEY_PREF_PRINTER_BAUD_RATE = "printer_wintec_baud_rate";
	public static final String KEY_PREF_BLUETOOTH_PRINTER = "bluetooth_printer";
	public static final String KEY_PREF_MSR_DEV_PATH = "msr_wintec_dev_path";
	public static final String KEY_PREF_MSR_BAUD_RATE = "msr_wintec_baud_rate";
	public static final String KEY_PREF_DSP_DEV_PATH = "dsp_wintec_dev_path";
	public static final String KEY_PREF_DSP_BAUD_RATE = "dsp_wintec_baud_rate";
	public static final String KEY_PREF_DRW_DEV_PATH = "drw_wintec_dev_path";
	public static final String KEY_PREF_DRW_BAUD_RATE = "drw_wintec_baud_rate";
	public static final String KEY_PREF_SHOW_MENU_IMG = "show_menu_image";
	public static final String KEY_PREF_SECOND_DISPLAY_IP = "second_display_ip";
	public static final String KEY_PREF_SECOND_DISPLAY_PORT = "second_display_port";
	public static final String KEY_PREF_ENABLE_DSP = "enable_dsp";
	public static final String KEY_PREF_DSP_TEXT_LINE1 = "dsp_wintec_line1";
	public static final String KEY_PREF_DSP_TEXT_LINE2 = "dsp_wintec_line2";
	public static final String KEY_PREF_ENABLE_SECOND_DISPLAY = "enable_second_display";
	public static final String KEY_PREF_LANGUAGE_LIST = "language_list";
	public static final String KEY_PREF_ENABLE_BACKUP_DB = "enable_backup_db";
	public static final String KEY_PREF_MONTHS_TO_KEEP_SALE = "months_keep_sale";
	public static final String KEY_PREF_THIRD_PARTY_NAME1 = "third_party_app_name1";
	public static final String KEY_PREF_THIRD_PARTY_URL1 = "third_party_app_url1";
	public static final String KEY_PREF_THIRD_PARTY_NAME2 = "third_party_app_name2";
	public static final String KEY_PREF_THIRD_PARTY_URL2 = "third_party_app_url2";
	public static final String KEY_PREF_THIRD_PARTY_NAME3 = "third_party_app_name3";
	public static final String KEY_PREF_THIRD_PARTY_URL3 = "third_party_app_url3";
	public static final String KEY_PREF_THIRD_PARTY_NAME4 = "third_party_app_name4";
	public static final String KEY_PREF_THIRD_PARTY_URL4 = "third_party_app_url4";
	public static final String KEY_PREF_THIRD_PARTY_NAME5 = "third_party_app_name5";
	public static final String KEY_PREF_THIRD_PARTY_URL5 = "third_party_app_url5";
	public static final String KEY_BT_PRINTER_MAC_ADDRESS = "bt_printer_mac_address";

	// store update information
	public static final String KEY_PREF_NEED_TO_UPDATE = "need_to_update";
	public static final String KEY_PREF_NEW_VERSION = "new_version";
	public static final String KEY_PREF_FILE_URL = "file_url";
	public static final String KEY_PREF_APK_DOWNLOAD_STATUS = "apk_download_status"; 	// 0 fail, 1 success
	public static final String KEY_PREF_APK_DOWNLOAD_FILE_NAME = "apk_download_file_name";
	public static final String KEY_PREF_APK_MD5 = "apk_md5";
	public static final String KEY_PREF_LAST_UPDATE = "last_update";
	public static final String KEY_PREF_EXP_DATE = "software_exp_date";
	public static final String KEY_PREF_LOCK_DATE = "software_lock_date";
	public static final String KEY_PREF_AUTO_SEND_ENDDAY = "auto_send_last_endday";
	public static final String KEY_PREF_SEND_SALE_REAL_TIME = "send_real_time";
	private static final int REQUEST_FOR_BLUETOOTH_SETTING = 100;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.pref_headers, target);
	}

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = 
			new Preference.OnPreferenceChangeListener() {
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} else {
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	private static void bindPreferenceSummaryToValue(Preference preference) {
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	protected boolean isValidFragment(String fragmentName) {
		return PreferenceFragment.class.getName().equals(fragmentName)
				|| PrinterPreferenceFragment.class.getName().equals(fragmentName)
				|| WintecSettingFragment.class.getName().equals(fragmentName)
				|| ConnectionPreferenceFragment.class.getName().equals(fragmentName)
				|| GeneralPreferenceFragment.class.getName().equals(fragmentName)
				|| SecondDisplayPreferenceFragment.class.getName().equals(fragmentName)
				|| ThirdPartyLinkFragment.class.getName().equals(fragmentName);
	}

	public static class PrinterPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener,
		BluetoothPrinterListDialogFragment.OnSelectedPrinterListener{

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_printer);
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_IP));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_LIST));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_DEV_PATH));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_PRINTER_BAUD_RATE));

			CheckBoxPreference prefBluetoothPrinter = (CheckBoxPreference) findPreference(KEY_PREF_BLUETOOTH_PRINTER);
			prefBluetoothPrinter.setOnPreferenceClickListener(this);
			setDependBluetoothPrinter();
		}

		private void setCheckedCheckBoxPreference(boolean isChecked){
			CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(KEY_PREF_BLUETOOTH_PRINTER);
			checkBoxPreference.setChecked(isChecked);
			setDependBluetoothPrinter();
		}

		private void setDependBluetoothPrinter(){
			if(Utils.isEnableBluetoothPrinter(getActivity())){
				setPrinterIpPreferenceEnable(false);
				setPrinterNamePreferenceEnable(false);
			}else{
				setPrinterIpPreferenceEnable(true);
				setPrinterNamePreferenceEnable(true);
			}
		}

		private void setPrinterNamePreferenceEnable(boolean isEnable){
			findPreference(KEY_PREF_PRINTER_LIST).setEnabled(isEnable);
		}

		private void setPrinterIpPreferenceEnable(boolean isEnable){
			findPreference(KEY_PREF_PRINTER_IP).setEnabled(isEnable);
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			if(requestCode == REQUEST_FOR_BLUETOOTH_SETTING){
				if(isBluetoothEnable()){
					showBluetoothSelectionFragment();
				}
			}
		}

		private void showBluetoothSelectionFragment(){
			BluetoothPrinterListDialogFragment btPrinterFragment =
					new BluetoothPrinterListDialogFragment();
			btPrinterFragment.setOnSelectedPrinterListener(this);
			btPrinterFragment.show(getFragmentManager(), BluetoothPrinterListDialogFragment.TAG);
		}

		private void saveBluetoothPrinterSetting(boolean isEnableBluetoothPrinter, String bluetoothAddress){
			final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
			if(isEnableBluetoothPrinter){
				sharedPref.edit().putString(KEY_BT_PRINTER_MAC_ADDRESS, bluetoothAddress).apply();
			}else{
				sharedPref.edit().putString(KEY_BT_PRINTER_MAC_ADDRESS, "").apply();
			}
		}

		private boolean isBluetoothEnable(){
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			return bluetoothAdapter.isEnabled();
		}

		private void gotoBluetoothSetting(){
			Intent intentBluetooth = new Intent();
			intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
			startActivityForResult(intentBluetooth, REQUEST_FOR_BLUETOOTH_SETTING);
		}

		@Override
		public void onSelectedPrinter(String bluetoothLogicalName, String bluetoothAddress) {
			if(!TextUtils.isEmpty(bluetoothAddress)){
				setCheckedCheckBoxPreference(true);
				saveBluetoothPrinterSetting(true, bluetoothAddress);
			}else{
				setCheckedCheckBoxPreference(false);
				saveBluetoothPrinterSetting(false, "");
			}
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			if(!isBluetoothEnable()) {
				gotoBluetoothSetting();
			} else {
				if(!TextUtils.isEmpty(Utils.getBluetoothAddress(getActivity()))) {
					showBluetoothSelectionFragment();
				}else{
					gotoBluetoothSetting();
				}
			}
			return true;
		}
	}
	
	public static class WintecSettingFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_wintec);
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_DRW_DEV_PATH));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_DRW_BAUD_RATE));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_MSR_DEV_PATH));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_MSR_BAUD_RATE));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_DSP_DEV_PATH));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_DSP_BAUD_RATE));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_DSP_TEXT_LINE1));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_DSP_TEXT_LINE2));
		}
	}
	
	public static class ConnectionPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_connection);
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_SERVER_URL));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_CONN_TIME_OUT_LIST));
		}
	}
	
	public static class GeneralPreferenceFragment extends PreferenceFragment{

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_MONTHS_TO_KEEP_SALE));
		}
		
	}
	
	public static class SecondDisplayPreferenceFragment extends PreferenceFragment{

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_second_display);
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_SECOND_DISPLAY_IP));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_SECOND_DISPLAY_PORT));
		}
		
	}

	public static class ThirdPartyLinkFragment extends PreferenceFragment{

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_third_party_link);
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_NAME1));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_URL1));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_NAME2));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_URL2));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_NAME3));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_URL3));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_NAME4));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_URL4));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_NAME5));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_THIRD_PARTY_URL5));
		}
	}

	public void dspTestClick(final View v){
		WintecCustomerDisplay dsp = new WintecCustomerDisplay(getApplicationContext());
		dsp.displayWelcome();
	}
	
	public void drwTestClick(final View v){
		WintecCashDrawer drw = new WintecCashDrawer(getApplicationContext());
		drw.openCashDrawer();
	}

	public void printTestClick(final View v){
		if(Utils.isInternalPrinterSetting(getApplicationContext())){
			WinTecTestPrint wt = new WinTecTestPrint(getApplicationContext());
			wt.print();
		}else{
			EPSONTestPrint ep;
			try {
				ep = new EPSONTestPrint(getApplicationContext());
				ep.print();
			} catch (final EposException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						String message = Utils.getEposExceptionText(e.getErrorStatus());
						Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
	}
	
	public static class WinTecTestPrint extends WintecPrinter{
		
		public WinTecTestPrint(Context context){
			super(context);
			mTextToPrint.append(mContext.getString(R.string.print_test_text).replaceAll("\\*", " "));
		}
	}
	
	public static class EPSONTestPrint extends EPSONPrinter{

		public EPSONTestPrint(Context context) throws EposException {
			super(context);
			mTextToPrint.append(mContext.getString(R.string.print_test_text).replaceAll("\\*", " "));
		}
	}
}

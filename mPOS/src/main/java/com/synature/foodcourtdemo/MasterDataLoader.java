package com.synature.foodcourtdemo;

import org.ksoap2.serialization.PropertyInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.synature.foodcourtdemo.datasource.BankDataSource;
import com.synature.foodcourtdemo.datasource.CashInOutDao;
import com.synature.foodcourtdemo.datasource.CashInOutDataSource;
import com.synature.foodcourtdemo.datasource.ComputerDataSource;
import com.synature.foodcourtdemo.datasource.CreditCardDataSource;
import com.synature.foodcourtdemo.datasource.GlobalPropertyDataSource;
import com.synature.foodcourtdemo.datasource.HeaderFooterReceiptDataSource;
import com.synature.foodcourtdemo.datasource.LanguageDataSource;
import com.synature.foodcourtdemo.datasource.MenuCommentDataSource;
import com.synature.foodcourtdemo.datasource.PaymentAmountButtonDataSource;
import com.synature.foodcourtdemo.datasource.PaymentDetailDataSource;
import com.synature.foodcourtdemo.datasource.ProductPriceDataSource;
import com.synature.foodcourtdemo.datasource.ProductsDataSource;
import com.synature.foodcourtdemo.datasource.ProgramFeatureDataSource;
import com.synature.foodcourtdemo.datasource.PromotionDiscountDataSource;
import com.synature.foodcourtdemo.datasource.ShopDataSource;
import com.synature.foodcourtdemo.datasource.StaffsDataSource;
import com.synature.pos.MasterData;
import com.synature.util.Logger;

import java.util.Calendar;

public class MasterDataLoader extends MPOSServiceBase{
	
	public static final String LOAD_MASTER_METHOD = "WSmPOS_JSON_LoadShopMasterData";

	/**
	 * @param context
	 * @param receiver
	 */
	public MasterDataLoader(Context context, int shopId, ResultReceiver receiver) {
		super(context, LOAD_MASTER_METHOD, receiver);
		
		// shopId
		mProperty = new PropertyInfo();
		mProperty.setName(SHOP_ID_PARAM);
		mProperty.setValue(shopId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
	}

	@Override
	protected void onPostExecute(String result) {
		Gson gson = new Gson();
		try {
			MasterData master = gson.fromJson(result, MasterData.class);
			updateMasterData(master);
		} catch (JsonSyntaxException e) {
			if(mReceiver != null){
				Bundle b = new Bundle();
				b.putString("msg", e.getMessage());
				mReceiver.send(RESULT_ERROR, b);
			}
		}
	}

	private void updateMasterData(MasterData master){
		ShopDataSource shop = new ShopDataSource(mContext);
		ComputerDataSource computer = new ComputerDataSource(mContext);
		GlobalPropertyDataSource format = new GlobalPropertyDataSource(mContext);
		StaffsDataSource staff = new StaffsDataSource(mContext);
		LanguageDataSource lang = new LanguageDataSource(mContext);
		HeaderFooterReceiptDataSource hf = new HeaderFooterReceiptDataSource(mContext);
		BankDataSource bank = new BankDataSource(mContext);
		CreditCardDataSource cd = new CreditCardDataSource(mContext);
		PaymentDetailDataSource pd = new PaymentDetailDataSource(mContext);
		PaymentAmountButtonDataSource pb = new PaymentAmountButtonDataSource(mContext);
		ProductsDataSource p = new ProductsDataSource(mContext);
		ProductPriceDataSource pp = new ProductPriceDataSource(mContext);
		MenuCommentDataSource mc = new MenuCommentDataSource(mContext);
		PromotionDiscountDataSource promo = new PromotionDiscountDataSource(mContext);
		ProgramFeatureDataSource feature = new ProgramFeatureDataSource(mContext);
        CashInOutDao cashInout = new CashInOutDataSource(mContext);
		try {
			shop.insertShopProperty(master.getShopProperty());
			computer.insertComputer(master.getComputerProperty());
			format.insertProperty(master.getGlobalProperty());
			staff.insertStaff(master.getStaffs());
			staff.insertStaffPermission(master.getStaffPermission());
			lang.insertLanguage(master.getLanguage());
			hf.insertHeaderFooterReceipt(master.getHeaderFooterReceipt());
			bank.insertBank(master.getBankName());
			cd.insertCreditCardType(master.getCreditCardType());
			pd.insertPaytype(master.getPayType());
			pd.insertPaytypeFinishWaste(master.getPayTypeFinishWaste());
			pb.insertPaymentAmountButton(master.getPaymentAmountButton());
			p.insertProductGroup(master.getProductGroup());
			p.insertProductDept(master.getProductDept());
			p.insertProducts(master.getProducts());
			pp.insertProductPrice(master.getProductPrice());
			p.insertPComponentGroup(master.getPComponentGroup());
			p.insertProductComponent(master.getProductComponent());
			mc.insertMenuFixComment(master.getMenuFixComment());
			promo.insertPromotionPriceGroup(master.getPromotionPriceGroup());
			promo.insertPromotionProductDiscount(master.getPromotionProductDiscount());
			feature.insertProgramFeature(master.getProgramFeature());
            cashInout.insertCashInOut(master.getCashInOut());

            setSyncStatus(true, String.valueOf(Calendar.getInstance().getTimeInMillis()));
			if(mReceiver != null)
				mReceiver.send(RESULT_SUCCESS, null);
		} catch (Exception e) {
			Logger.appendLog(mContext, MPOSApplication.LOG_PATH, 
					MPOSApplication.LOG_FILE_NAME, 
					"Error when add shop data : " + e.getMessage());
            setSyncStatus(false, "");
			if(mReceiver != null){
				Bundle b = new Bundle();
				b.putString("msg", e.getMessage());
				mReceiver.send(RESULT_ERROR, b);
			}
		}
	}

    private void setSyncStatus(boolean isAlreadySync, String syncTime){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SettingsActivity.KEY_PREF_IS_SYNC, isAlreadySync);
        editor.putString(SettingsActivity.KEY_PREF_SYNC_TIME, syncTime);
        editor.commit();
    }
}

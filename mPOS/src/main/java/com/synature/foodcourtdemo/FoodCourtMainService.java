package com.synature.foodcourtdemo;

import java.lang.reflect.Type;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.synature.pos.PrepaidCardInfo;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

public abstract class FoodCourtMainService extends MPOSServiceBase{

	public static final String GET_BALANCE_METHOD = "WSiFoodCourt_FC_GetBalanceAmountFromCardNo";//"WSiFoodCourt_JSON_GetBalanceAmountFromCardNo";
	public static final String PAY_METHOD = "WSiFoodCourt_FC_PayAmountOfCardNo";//"WSiFoodCourt_JSON_PayAmountOfCardNo";
    public static final String PAY_MULTI_CARD_METHOD = "WSiFoodCourt_FC_PayAmountMultiCardNo";
	
	public static final String CARD_NO_PARAM = "szCardNo";
	public static final String PAY_AMOUNT_PARAM = "fPayAmount";
    public static final String LIST_CARD_NO_PARAM = "szListCardNo";
    public static final String LIST_PAY_AMOUNT_PARAM = "szListPayAmount";

    protected String mCardNo;

	public FoodCourtMainService(Context context, String method, int shopId, 
			int computerId, int staffId, String cardNo, ResultReceiver receiver) {
		super(context, method, receiver);

        mCardNo = cardNo;

		mProperty = new PropertyInfo();
		mProperty.setName(SHOP_ID_PARAM);
		mProperty.setValue(shopId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);

		mProperty = new PropertyInfo();
		mProperty.setName(COMPUTER_ID_PARAM);
		mProperty.setValue(computerId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);

		mProperty = new PropertyInfo();
		mProperty.setName(STAFF_ID_PARAM);
		mProperty.setValue(staffId);
		mProperty.setType(int.class);
		mSoapRequest.addProperty(mProperty);
	}

    protected void setErrorReceiver(String msg){
        if(mReceiver != null) {
            Bundle b = new Bundle();
            b.putString("msg", msg);
            mReceiver.send(RESULT_ERROR, b);
        }
    }

    protected void setSuccessReceiver(String cardNo, double cardBalance){
        if(mReceiver != null){
            Bundle b = new Bundle();
            b.putString("cardNo", cardNo);
            b.putDouble("cardBalance", cardBalance);
            mReceiver.send(RESULT_SUCCESS, b);
        }
    }

	protected PrepaidCardInfo toPrepaidCardInfoObject(String json) throws JsonSyntaxException{
		Gson gson = new Gson();
		Type type = new TypeToken<PrepaidCardInfo>(){}.getType();
		PrepaidCardInfo card = gson.fromJson(json, type);
		return card;
	}
}

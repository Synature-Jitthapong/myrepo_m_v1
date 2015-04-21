package com.synature.foodcourtdemo;

import com.google.gson.JsonSyntaxException;
import com.synature.pos.WebServiceResult;

import android.content.Context;
import android.os.ResultReceiver;
import android.text.TextUtils;

import org.ksoap2.serialization.PropertyInfo;

public class FoodCourtBalanceOfCard extends FoodCourtMainService{

	/**
	 * @param context
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param cardNo
	 * @param receiver
	 */
	public FoodCourtBalanceOfCard(Context context, int shopId,
			int computerId, int staffId, String cardNo, 
			ResultReceiver receiver) {
		super(context, GET_BALANCE_METHOD, shopId, computerId, staffId, cardNo, null);
		mReceiver = receiver;

        mProperty = new PropertyInfo();
        mProperty.setName(CARD_NO_PARAM);
        mProperty.setValue(cardNo);
        mProperty.setType(String.class);
        mSoapRequest.addProperty(mProperty);
	}

	@Override
	protected void onPostExecute(String result) {
		WebServiceResult ws;
		try {
			ws = toServiceObject(result);
			if(ws.getiResultID() == RESPONSE_SUCCESS){
				try {
					setSuccessReceiver(mCardNo, Double.parseDouble(ws.getSzResultData()));
				} catch (Exception e) {
					setErrorReceiver(e.getMessage());
				}
			}else{
				setErrorReceiver(TextUtils.isEmpty(ws.getSzResultData()) ? result : ws.getSzResultData());
			}
		} catch (JsonSyntaxException e) {
			setErrorReceiver(result);
		}
	}
}

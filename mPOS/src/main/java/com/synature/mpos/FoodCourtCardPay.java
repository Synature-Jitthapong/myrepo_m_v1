package com.synature.mpos;

import org.ksoap2.serialization.PropertyInfo;

import com.google.gson.JsonSyntaxException;
import com.synature.pos.PrepaidCardInfo;
import com.synature.pos.WebServiceResult;

import android.content.Context;
import android.os.ResultReceiver;
import android.text.TextUtils;

public class FoodCourtCardPay extends FoodCourtMainService{

	/**
	 * @param context
	 * @param shopId
	 * @param computerId
	 * @param staffId
	 * @param cardNo
	 * @param payAmount
	 * @param receiver
	 */
	public FoodCourtCardPay(Context context, int shopId,
			int computerId, int staffId, String cardNo, String payAmount, 
			ResultReceiver receiver) {
		super(context, PAY_METHOD, shopId, computerId, staffId, cardNo, null);

		mReceiver = receiver;
		
		mProperty = new PropertyInfo();
		mProperty.setName(PAY_AMOUNT_PARAM);
		mProperty.setValue(payAmount);
		mProperty.setType(String.class);
		mSoapRequest.addProperty(mProperty);
	}

	@Override
	protected void onPostExecute(String result) {
//		WebServiceResult ws;
//		try {
//			ws = toServiceObject(result);
//			if(ws.getiResultID() == RESPONSE_SUCCESS){
//				try {
//					PrepaidCardInfo cardInfo = toPrepaidCardInfoObject(ws.getSzResultData());
//					setSuccessReceiver(cardInfo);
//				} catch (Exception e) {
//					setErrorReceiver(e.getMessage());
//				}
//			}else{
//				setErrorReceiver(TextUtils.isEmpty(ws.getSzResultData()) ? result : ws.getSzResultData());
//			}
//		} catch (JsonSyntaxException e) {
//			setErrorReceiver(result);
//		}
        setSuccessReceiver(FoodCourtBalanceOfCard.toCardInfoObj());
	}

}

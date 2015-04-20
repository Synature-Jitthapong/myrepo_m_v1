package com.synature.mpos;

import com.google.gson.JsonSyntaxException;
import com.synature.pos.PrepaidCardInfo;
import com.synature.pos.WebServiceResult;

import android.content.Context;
import android.os.ResultReceiver;
import android.text.TextUtils;

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
        setSuccessReceiver(toCardInfoObj());
	}

    public static PrepaidCardInfo toCardInfoObj(){
        PrepaidCardInfo cardInfo = new PrepaidCardInfo();
        cardInfo.setiCardID(1);
        cardInfo.setiCardStatus(FoodCourtPayActivity.STATUS_READY_TO_USE);
        cardInfo.setSzCardNo("1111-2222-3333-4444");
        cardInfo.setfCurrentAmount(30);
        return cardInfo;
    }
}

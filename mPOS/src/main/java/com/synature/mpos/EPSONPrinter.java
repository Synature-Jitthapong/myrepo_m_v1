package com.synature.mpos;

import android.content.Context;

import com.epson.eposprint.BatteryStatusChangeEventListener;
import com.epson.eposprint.Builder;
import com.epson.eposprint.EposException;
import com.epson.eposprint.Print;
import com.epson.eposprint.StatusChangeEventListener;
import com.epson.epsonio.EpsonIoException;
import com.synature.util.LevelTextPrint;
import com.synature.util.LevelTextPrint.ThreeLevelByteCode;

public class EPSONPrinter extends PrinterBase implements
        BatteryStatusChangeEventListener, StatusChangeEventListener {

    protected Context mContext;
    protected Print mPrinter;
    protected Builder mBuilder;

    /**
     * @param context
     */
    public EPSONPrinter(Context context) throws EposException {
        super(context);
        mContext = context;
        mPrinter = new Print(context.getApplicationContext());

        if (mPrinter != null) {
            mPrinter.setStatusChangeEventCallback(this);
            mPrinter.setBatteryStatusChangeEventCallback(this);
            String deviceName = Utils.getPrinterIp(context);
            String modelName = Utils.getEPSONModelName(mContext);
            int deviceType = Print.DEVTYPE_TCP;
            int font = Builder.FONT_B;
            if (Utils.isEnableBluetoothPrinter(context)) {
                deviceType = Print.DEVTYPE_BLUETOOTH;
                deviceName = Utils.getBluetoothAddress(context);
                modelName = "TM-m30";
                font = Builder.FONT_A;
            }
            if (open(deviceType, deviceName)) {
                mBuilder = new Builder(modelName, Builder.MODEL_ANK, mContext);
                mBuilder.addTextSize(1, 1);
                mBuilder.addTextFont(font);
            }
        }
    }

    @Override
    public void print() {
        if(mPrinter == null || mBuilder == null)
            return;
        int[] status = new int[1];
        int[] battery = new int[1];
        try {
            createBuilder();
            mBuilder.addFeedUnit(30);
            mBuilder.addCut(Builder.CUT_FEED);
            mPrinter.sendData(mBuilder, 10000, status, battery);
        } catch (EposException e) {
            e.printStackTrace();
        }
        if (mBuilder != null) {
            mBuilder.clearCommandBuffer();
        }
        close();
    }

    private void createBuilder() throws EposException {
        mBuilder.addText(mTextToPrint.toString());
    }

    private boolean open(int deviceType, String deviceName) {
        try {
            mPrinter.openPrinter(deviceType, deviceName, Print.FALSE, Print.PARAM_DEFAULT);
            return true;
        } catch (EposException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void close() {
        try {
            mPrinter.closePrinter();
            mPrinter = null;
        } catch (EposException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChangeEvent(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBatteryStatusChangeEvent(String arg0, int arg1) {
        // TODO Auto-generated method stub

    }
}

package com.synature.mpos;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.synature.mpos.datasource.GlobalPropertyDataSource;
import com.synature.pos.GlobalProperty;

public class MyDigitalClock extends TextView {
    private Calendar mCalendar;
    private Runnable mTicker;
    private Handler mHandler;
    private String mFormat;
    private SimpleDateFormat mSimFormat;
    private boolean mTickerStopped = false;

    public MyDigitalClock(Context context) {
        super(context);
        initClock(context);
    }

    public MyDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    private void initClock(Context context) {
        mCalendar = Utils.getCalendar();
        GlobalPropertyDataSource global = new GlobalPropertyDataSource(context);
        GlobalProperty globalProperty = global.getGlobalProperty();
        if(!TextUtils.isEmpty(globalProperty.getDateFormat())
                && !TextUtils.isEmpty(globalProperty.getTimeFormat())) {
            mFormat = globalProperty.getDateFormat()
                    + " " + globalProperty.getTimeFormat();
            mSimFormat = new SimpleDateFormat(mFormat);
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
            public void run() {
                if (mTickerStopped) return;
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                if(mSimFormat != null) {
                    setText(mSimFormat.format(mCalendar.getTime()));
                }else{
                    setText(DateFormat.getDateTimeInstance().format(mCalendar.getTime()));
                }
                invalidate();
                long now = SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }
}
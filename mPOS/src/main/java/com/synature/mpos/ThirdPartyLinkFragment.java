package com.synature.mpos;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.synature.pos.Product;

/**
 * Created by j1tth4 on 6/16/15.
 */
public class ThirdPartyLinkFragment extends DialogFragment {

    public static final String TAG = "ThirdPartyLinkFragment";

    private WebView mWebView;
    private ImageButton mBtnClose;
    private ProgressBar mLoadingProgress;

    private int mLinkId = 0x101;

    public static ThirdPartyLinkFragment newInstance(int linkId){
        ThirdPartyLinkFragment f = new ThirdPartyLinkFragment();
        Bundle b = new Bundle();
        b.putInt("linkId", linkId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLinkId = getArguments().getInt("linkId");

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomDialogNoTitle);
        setCancelable(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        int height = getResources().getDimensionPixelSize(R.dimen.dialog_height);

        getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWebView = (WebView) view.findViewById(R.id.thirdPartyWebView);
        mBtnClose = (ImageButton) view.findViewById(R.id.btnClose);
        mLoadingProgress = (ProgressBar) view.findViewById(R.id.loadingProgress);

        mBtnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mLoadingProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                mLoadingProgress.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }
        });
        mWebView.loadUrl(getUrlFromLinkId());
    }

    private String getUrlFromLinkId(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String link = null;
        if(mLinkId == 0x101){
            link = preferences.getString(SettingsActivity.KEY_PREF_THIRD_PARTY_URL1, "");
        }
        if(mLinkId == 0x102){
            link = preferences.getString(SettingsActivity.KEY_PREF_THIRD_PARTY_URL2, "");
        }
        if(mLinkId == 0x103){
            link = preferences.getString(SettingsActivity.KEY_PREF_THIRD_PARTY_URL3, "");
        }
        if(mLinkId == 0x104){
            link = preferences.getString(SettingsActivity.KEY_PREF_THIRD_PARTY_URL4, "");
        }
        if(mLinkId == 0x105){
            link = preferences.getString(SettingsActivity.KEY_PREF_THIRD_PARTY_URL5, "");
        }
        return Utils.checkProtocal(link);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.third_party_webview, container, false);
    }
}

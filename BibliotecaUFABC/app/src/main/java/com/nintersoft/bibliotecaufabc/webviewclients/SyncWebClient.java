package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.SyncActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalVariables;

public class SyncWebClient extends WebViewClient {
    private int reserve_url_loaded;
    private int login_page_finished;
    private int login_home_finished;

    private String user_login;
    private String user_password;

    private Context mContext;

    public SyncWebClient(Context context){
        super();
        mContext = context;

        reserve_url_loaded = 0;
        login_page_finished = 0;
        login_home_finished = 0;

        loadSettings();
    }

    private void loadSettings(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        GlobalVariables.storeUserFormData = pref.getBoolean(mContext.getString(R.string.key_privacy_store_password), true);

        user_login = pref.getString(mContext.getString(R.string.key_privacy_login_username), "");
        user_password = pref.getString(mContext.getString(R.string.key_privacy_login_password), "");

        //TODO: Schedule notification for the next day
        if (user_login == null || user_password == null) ((SyncActivity)mContext).finish();
        if (!GlobalVariables.storeUserFormData || user_login.isEmpty()
                || user_password.isEmpty()) ((SyncActivity)mContext).finish();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        //TODO: Perform synchronization
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        //TODO: Schedule notification for the next day
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        //TODO: Schedule notification for the next day
    }
}

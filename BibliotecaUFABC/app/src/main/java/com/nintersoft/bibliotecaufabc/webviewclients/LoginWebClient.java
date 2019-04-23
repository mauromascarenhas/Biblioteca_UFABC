package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.LoginActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

import androidx.annotation.RequiresApi;

public class LoginWebClient extends WebViewClient {
    private int login_page_finished;
    private int login_home_finished;

    private Context mContext;

    public LoginWebClient(Context context){
        super();
        mContext = context;
        login_page_finished = 0;
        login_home_finished = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (url.contains(GlobalConstants.URL_ACCESS_PAGE)){
            if (login_page_finished == 0){
                if (GlobalConstants.storeUserFormData)
                    ((LoginActivity)mContext).setSavedUserLogin();
                else
                    ((LoginActivity)mContext).setupInterface(true);
                login_page_finished++;
            }

            String script = String.format("javascript: %1$s \ncheckForErrors();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/login_scraper.js"));
            GlobalFunctions.executeScript(view, script);
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_HOME)){
            if (login_home_finished < 1){
                login_home_finished++;
                return;
            }

            String script = String.format("javascript: %1$s \ngetUsername();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/login_scraper.js"));
            GlobalFunctions.executeScript(view, script);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        ((LoginActivity)mContext).setErrorForm(error.getDescription().toString());
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        ((LoginActivity)mContext).setErrorForm(description);
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
}

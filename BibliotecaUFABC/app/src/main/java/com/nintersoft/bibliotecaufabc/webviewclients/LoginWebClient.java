package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.activities.LoginActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.utilities.GlobalVariables;

import androidx.annotation.RequiresApi;

public class LoginWebClient extends WebViewClient {
    private int login_page_finished;
    private int login_services_finished;

    private Context mContext;

    public LoginWebClient(Context context){
        super();
        mContext = context;
        login_page_finished = 0;
        login_services_finished = 0;
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

        if (url.contains(GlobalConstants.URL_LIBRARY_LOGIN_P)){
            if (login_page_finished == 0){
                if (GlobalVariables.storeUserFormData)
                    ((LoginActivity)mContext).setSavedUserLogin();
                else
                    ((LoginActivity)mContext).setupInterface(true);
                login_page_finished++;
            }
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_SERVICES)){
            if (login_services_finished < 1){
                login_services_finished++;
                return;
            }

            String script = String.format("%1$s \ngetUsername();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/login_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    ((LoginActivity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!value.isEmpty()) ((LoginActivity)mContext).hasLoggedIn(value);
                        }
                    });
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, final WebResourceError error) {
        super.onReceivedError(view, request, error);

        ((LoginActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((LoginActivity)mContext).setErrorForm(error.getDescription().toString());
            }
        });
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, final String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        ((LoginActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((LoginActivity)mContext).setErrorForm(description);
            }
        });
    }
}

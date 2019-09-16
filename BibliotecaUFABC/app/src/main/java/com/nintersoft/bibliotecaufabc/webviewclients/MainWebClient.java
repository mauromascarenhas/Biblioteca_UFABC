package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.activities.MainActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

public class MainWebClient extends WebViewClient {
    private int home_page_loaded;

    private Context mContext;

    public MainWebClient(Context context){
        mContext = context;
        home_page_loaded = 0;
    }

    public void resetCounters(){
        home_page_loaded = 0;
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
        if (home_page_loaded  < 1){
            home_page_loaded++;
            return;
        }

        String script = String.format("javascript: %1$s\ncheckLoginStatus();",
                GlobalFunctions.getScriptFromAssets(mContext, "javascript/main_scraper.js"));
        view.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String value) {
                ((MainActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject result = new JSONObject(value);
                            ((MainActivity)mContext).setUserConnected(result.getBoolean("status"),
                                    result.getString("name"));
                        }
                        catch (JSONException e){
                            ((MainActivity)mContext).setUserConnected(false, "");
                        }
                    }
                });
            }
        });

        script = String.format("%1$s\ngetNewestBooks();",
                GlobalFunctions.getScriptFromAssets(mContext, "javascript/main_scraper.js"));
        view.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String value) {
                ((MainActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity)mContext).setSearchResults(value);
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, final WebResourceError error) {
        super.onReceivedError(view, request, error);

        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity)mContext).receiveError(error.getDescription().toString());
            }
        });
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, final String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity)mContext).receiveError(description);
            }
        });
    }
}

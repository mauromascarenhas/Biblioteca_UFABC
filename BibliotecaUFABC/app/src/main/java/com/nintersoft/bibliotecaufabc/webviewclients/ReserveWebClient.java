package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.BookViewerActivity;
import com.nintersoft.bibliotecaufabc.constants.GlobalConstants;

import androidx.annotation.RequiresApi;

public class ReserveWebClient extends WebViewClient {
    private int reserve_url_loaded;

    private Context mContext;

    public ReserveWebClient(Context context){
        this.mContext = context;
        reserve_url_loaded = 0;
    }

    public void resetCounters(){
        reserve_url_loaded = 0;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (url.contains(GlobalConstants.URL_LIBRARY_RESERVE)){
            if (reserve_url_loaded < 1){
                reserve_url_loaded++;
                return;
            }

            String script = String.format("javascript: %1$s\ndetectAction()",
                    GlobalConstants.getScriptFromAssets(mContext,
                            "javascript/reserve_scraper.js"));
            GlobalConstants.executeScript(view, script);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        ((BookViewerActivity)mContext).setReservationError(error.getDescription().toString());
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        ((BookViewerActivity)mContext).setReservationError(description);
    }
}

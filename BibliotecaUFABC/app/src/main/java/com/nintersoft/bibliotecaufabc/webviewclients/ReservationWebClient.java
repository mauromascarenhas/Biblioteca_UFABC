package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.ReservationActivity;
import com.nintersoft.bibliotecaufabc.constants.GlobalConstants;

import androidx.annotation.RequiresApi;

public class ReservationWebClient extends WebViewClient {
    private int reservation_page_finished;
    private int confirmation_page_finished;

    private Context mContext;

    public ReservationWebClient(Context context){
        super();
        this.mContext = context;
        reservation_page_finished = 0;
        confirmation_page_finished = 0;
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

        if (url.contains(GlobalConstants.URL_LIBRARY_CANCEL_RESERVATION)){
            if (confirmation_page_finished < 1){
                confirmation_page_finished++;
                return;
            }

            String script = String.format("javascript: %1$s \ngetCancellationMessage();",
                    GlobalConstants.getScriptFromAssets(mContext, "javascript/reservation_scraper.js"));
            GlobalConstants.executeScript(view, script);
            reservation_page_finished = 0;
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_RESERVATION)){
            if (reservation_page_finished < 1){
                reservation_page_finished++;
                return;
            }

            String script = String.format("javascript: %1$s \ngetReservations();",
                    GlobalConstants.getScriptFromAssets(mContext, "javascript/reservation_scraper.js"));
            GlobalConstants.executeScript(view, script);
            confirmation_page_finished = 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        ((ReservationActivity)mContext).setErrorForm(error.getDescription().toString());
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        ((ReservationActivity)mContext).setErrorForm(description);
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
}

package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.ReservationActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

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

            String script = String.format("%1$s \ngetCancellationMessage();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/reservation_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    ((ReservationActivity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ReservationActivity)mContext).setupInterface(false);
                            ((ReservationActivity)mContext).showCancellationMessage(value);
                        }
                    });
                }
            });
            reservation_page_finished = 0;
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_RESERVATION)){
            if (reservation_page_finished < 1){
                reservation_page_finished++;
                return;
            }

            String script = String.format("%1$s \ngetReservations();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/reservation_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    ((ReservationActivity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                JSONObject result = new JSONObject(value);
                                if (result.getBoolean("hasErrorDiv"))
                                    ((ReservationActivity)mContext).setUserNameNoReservation(result.getString("username"));
                                else {
                                    ((ReservationActivity)mContext).setReservationBooks(result.getJSONArray("reservationBooks").toString());
                                    ((ReservationActivity)mContext).setupInterface(true);
                                }
                            } catch (JSONException e){
                                ((ReservationActivity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ReservationActivity)mContext).setUserNameNoReservation("???");
                                    }
                                });
                            }
                        }
                    });
                }
            });
            confirmation_page_finished = 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, final WebResourceError error) {
        super.onReceivedError(view, request, error);

        ((ReservationActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ReservationActivity)mContext).setErrorForm(error.getDescription().toString());
            }
        });
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, final String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        ((ReservationActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ReservationActivity)mContext).setErrorForm(description);
            }
        });
    }
}

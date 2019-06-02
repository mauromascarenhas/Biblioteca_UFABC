package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.BookViewerActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

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

            String script = String.format("%1$s \ndetectAction()",
                    GlobalFunctions.getScriptFromAssets(mContext,
                            "javascript/reserve_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    ((BookViewerActivity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject result = new JSONObject(value);
                                if (result.getBoolean("hasData")){
                                    ((BookViewerActivity)mContext).setReservationAvailability(result.getJSONObject("data").toString());
                                }
                                else ((BookViewerActivity)mContext).setReservationResults(value);
                            } catch (JSONException e){
                                ((BookViewerActivity)mContext).setReservationResults("ERROR");
                            }
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

        ((BookViewerActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BookViewerActivity)mContext).setReservationError(error.getDescription().toString());
            }
        });
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, final String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        ((BookViewerActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BookViewerActivity)mContext).setReservationError(description);
            }
        });
    }
}

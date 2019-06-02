package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.RenewalActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

public class RenewalWebClient extends WebViewClient {
    private int renewal_page_finished;
    private int confirmation_page_finished;

    private Context mContext;

    public RenewalWebClient(Context context){
        super();
        this.mContext = context;
        renewal_page_finished = 0;
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
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (url.contains(GlobalConstants.URL_LIBRARY_PERFORM_RENEWAL)){
            if (confirmation_page_finished < 1){
                confirmation_page_finished++;
                return;
            }

            String script = String.format("%1$s \ngetRenewalMessage();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/renewed_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    ((RenewalActivity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((RenewalActivity)mContext).setupInterface(false);
                            ((RenewalActivity)mContext).showRenewalMessage(value);
                        }
                    });
                }
            });
            renewal_page_finished = 0;
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_RENEWAL)){
            if (renewal_page_finished < 1){
                renewal_page_finished++;
                return;
            }

            String script = String.format("%1$s \ngetRenewals();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/renewal_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    ((RenewalActivity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                JSONObject result = new JSONObject(value);
                                if (result.getBoolean("connected")){
                                    if (result.getBoolean("hasErrorDiv"))
                                        ((RenewalActivity)mContext).setUserNameNoRenewal(result.getString("usernameError"));
                                    else {
                                        ((RenewalActivity)mContext).setReservationBooks(result.getJSONObject("renewalBooks").toString());
                                        ((RenewalActivity)mContext).setupInterface(true);
                                    }
                                }
                                else ((RenewalActivity)mContext).setUserDisconnected();
                            } catch (JSONException e){
                                ((RenewalActivity)mContext).setUserDisconnected();
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

        ((RenewalActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RenewalActivity)mContext).setErrorForm(error.getDescription().toString());
            }
        });
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, final String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        ((RenewalActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RenewalActivity)mContext).setErrorForm(description);
            }
        });
    }
}

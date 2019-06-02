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

public class DetailsWebClient extends WebViewClient {
    private int book_page_finished;

    private Context mContext;

    public DetailsWebClient(Context context){
        super();
        this.mContext = context;
        this.book_page_finished = 0;
    }

    public void resetCounters(){
        this.book_page_finished = 0;
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

        if (book_page_finished < 1){
            book_page_finished++;
            return;
        }

        if (url.contains(GlobalConstants.URL_LIBRARY_DETAILS)){
            String script = String.format("%1$s \ngetBookDetails();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/details_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    ((BookViewerActivity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                JSONObject result = new JSONObject(value);
                                ((BookViewerActivity)mContext).setBookData(result.getString("details"),
                                        result.getBoolean("login"));
                                ((BookViewerActivity)mContext).setupInterface(true);
                            } catch (JSONException e){
                                ((BookViewerActivity)mContext).setErrorForm("UNKNOWN");
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
                ((BookViewerActivity)mContext).setErrorForm(error.getDescription().toString());
            }
        });
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, final String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        ((BookViewerActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BookViewerActivity)mContext).setErrorForm(description);
            }
        });
    }
}

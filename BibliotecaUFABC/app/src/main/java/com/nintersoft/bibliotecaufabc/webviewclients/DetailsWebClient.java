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

public class DetailsWebClient extends WebViewClient {
    private int book_page_finished = 0;

    private Context mContext;

    public DetailsWebClient(Context context){
        super();
        this.mContext = context;
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
            String script = String.format("javascript: %1$s \ngetBookDetails();",
                    GlobalConstants.getScriptFromAssets(mContext, "javascript/details_scraper.js"));
            GlobalConstants.executeScript(view, script);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        ((BookViewerActivity)mContext).setErrorForm(error.getDescription().toString());
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        ((BookViewerActivity)mContext).setErrorForm(description);
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
}
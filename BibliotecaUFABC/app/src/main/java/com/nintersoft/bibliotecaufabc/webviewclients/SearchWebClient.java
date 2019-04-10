package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.SearchActivity;
import com.nintersoft.bibliotecaufabc.constants.GlobalConstants;

import androidx.annotation.RequiresApi;

public class SearchWebClient extends WebViewClient {
    private int search_home_finished;
    private int search_search_finished;

    private Context mContext;

    public SearchWebClient(Context context){
        super();
        mContext = context;
        search_home_finished = 0;
        search_search_finished = 0;
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

        if (url.contains(GlobalConstants.URL_LIBRARY_HOME)) {
            if (search_home_finished == 0){
                search_home_finished++;
                return;
            }

            String searchQuery = ((SearchActivity) mContext).getSearchData();
            String script = String.format("javascript: %1$s\ndocumentReady();\nperformSearch(\"%2$s\");",
                    GlobalConstants.getScriptFromAssets(mContext, "javascript/search_scraper.js"),
                    searchQuery);
            GlobalConstants.executeScript(view, script);
            search_home_finished++;
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_SEARCH)){
            if (search_search_finished < 1){
                search_search_finished++;
                return;
            }

            ((SearchActivity)mContext).setupInterface(true);
            String script = String.format("javascript: %1$s\ngetSearchResults();",
                    GlobalConstants.getScriptFromAssets(mContext, "javascript/search_scraper.js"));
            GlobalConstants.executeScript(view, script);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        ((SearchActivity)mContext).setErrorForm(error.getDescription().toString());
        super.onReceivedError(view, request, error);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        ((SearchActivity)mContext).setErrorForm(description);
        super.onReceivedError(view, errorCode, description, failingUrl);
    }
}

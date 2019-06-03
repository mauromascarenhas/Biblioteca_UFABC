package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nintersoft.bibliotecaufabc.SearchActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

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
            String script = String.format("%1$s\ndocumentReady();\nperformSearch(\"%2$s\", \'%3$s\');",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/search_scraper.js"),
                    searchQuery, ((SearchActivity) mContext).JSONSearchFilters());
            view.evaluateJavascript(script, null);
            search_home_finished++;
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_SEARCH)){
            if (search_search_finished > 0) return;

            search_search_finished++;
            ((SearchActivity)mContext).setupInterface(true);
            String script = String.format("%1$s\ngetSearchResults();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/search_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    ((SearchActivity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((SearchActivity)mContext).setupInterface(true);
                            ((SearchActivity)mContext).setSearchResults(value);
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

        ((SearchActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SearchActivity)mContext).setErrorForm(error.getDescription().toString());
            }
        });
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, final String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        ((SearchActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SearchActivity)mContext).setErrorForm(description);
            }
        });
    }
}

package com.nintersoft.bibliotecaufabc.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.nintersoft.bibliotecaufabc.SearchActivity;

@SuppressWarnings("unused")
public class SearchJSInterface {
    private Context mContext;

    public SearchJSInterface(Context context){
        mContext = context;
    }

    @JavascriptInterface
    public void setSearchResults(final String results, final boolean hasMore){
        ((SearchActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SearchActivity)mContext).setupInterface(true);
                ((SearchActivity)mContext).setSearchResults(results, hasMore);
            }
        });
    }

    @JavascriptInterface
    public String getSearchFilters(){
        return ((SearchActivity) mContext).JSONSearchFilters();
    }
}

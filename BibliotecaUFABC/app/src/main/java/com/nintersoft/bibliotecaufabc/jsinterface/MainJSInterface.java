package com.nintersoft.bibliotecaufabc.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.nintersoft.bibliotecaufabc.MainActivity;

@SuppressWarnings("unused")
public class MainJSInterface {
    private Context mContext;

    public MainJSInterface(Context context){
        mContext = context;
    }

    @JavascriptInterface
    public void setBooks(final String books){
        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity)mContext).setSearchResults(books);
            }
        });
    }

    @JavascriptInterface
    public void setConnected(final boolean connected, final String user){
        ((MainActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity)mContext).setUserConnected(connected, user);
            }
        });
    }
}

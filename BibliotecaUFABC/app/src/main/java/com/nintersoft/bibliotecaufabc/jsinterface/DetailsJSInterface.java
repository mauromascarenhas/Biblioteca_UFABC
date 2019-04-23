package com.nintersoft.bibliotecaufabc.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.nintersoft.bibliotecaufabc.BookViewerActivity;

@SuppressWarnings("unused")
public class DetailsJSInterface {
    private Context mContext;

    public DetailsJSInterface(Context context){
        mContext = context;
    }

    @JavascriptInterface
    public void setBookDetails(final String details){
        ((BookViewerActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BookViewerActivity)mContext).setBookData(details);
                ((BookViewerActivity)mContext).setupInterface(true);
            }
        });
    }
}

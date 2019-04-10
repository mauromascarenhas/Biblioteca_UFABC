package com.nintersoft.bibliotecaufabc.jsinterface;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.nintersoft.bibliotecaufabc.BookViewerActivity;

public class ReserveJSInterface {
    private Context mContext;

    public ReserveJSInterface(Context context){
        mContext = context;
    }

    @JavascriptInterface
    public void getServerMessage(final String message){
        ((BookViewerActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BookViewerActivity)mContext).setReservationResults(message);
            }
        });
    }

    @JavascriptInterface
    public void setOptions(final String options){
        ((BookViewerActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((BookViewerActivity)mContext).setReservationAvailability(options);
            }
        });
    }

    @JavascriptInterface
    public String getOptions(){
        return ((BookViewerActivity)mContext).getTemporaryObject();
    }
}

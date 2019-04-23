package com.nintersoft.bibliotecaufabc.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.nintersoft.bibliotecaufabc.RenewalActivity;

@SuppressWarnings("unused")
public class RenewalJSInterface {
    private Context mContext;

    public RenewalJSInterface(Context context){
        mContext = context;
    }

    @JavascriptInterface
    public void setRenewalBooks(final String books){
        ((RenewalActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RenewalActivity)mContext).setReservationBooks(books);
                ((RenewalActivity)mContext).setupInterface(true);
            }
        });
    }

    @JavascriptInterface
    public void setUsernameErr(final String name){
        ((RenewalActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RenewalActivity)mContext).setUserNameNoRenewal(name);
            }
        });
    }

    @JavascriptInterface
    public void setConfirmationMessage(final String message){
        ((RenewalActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RenewalActivity)mContext).setupInterface(false);
                ((RenewalActivity)mContext).showRenewalMessage(message);
            }
        });
    }
}

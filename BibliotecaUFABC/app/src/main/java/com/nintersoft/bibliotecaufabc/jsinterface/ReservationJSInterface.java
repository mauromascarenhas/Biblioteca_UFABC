package com.nintersoft.bibliotecaufabc.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.nintersoft.bibliotecaufabc.ReservationActivity;

@SuppressWarnings("unused")
public class ReservationJSInterface {
    private Context mContext;

    public ReservationJSInterface(Context context){
        mContext = context;
    }

    @JavascriptInterface
    public void setReservationBooks(final String books){
        ((ReservationActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ReservationActivity)mContext).setReservationBooks(books);
                ((ReservationActivity)mContext).setupInterface(true);
            }
        });
    }

    @JavascriptInterface
    public void setUsernameErr(final String name){
        ((ReservationActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ReservationActivity)mContext).setUserNameNoReservation(name);
            }
        });
    }

    @JavascriptInterface
    public void setCancellationMessage(final String message){
        ((ReservationActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ReservationActivity)mContext).setupInterface(false);
                ((ReservationActivity)mContext).showCancellationMessage(message);
            }
        });
    }
}

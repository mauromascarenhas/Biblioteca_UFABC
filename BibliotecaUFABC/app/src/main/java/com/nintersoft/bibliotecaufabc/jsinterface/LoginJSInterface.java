package com.nintersoft.bibliotecaufabc.jsinterface;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.nintersoft.bibliotecaufabc.LoginActivity;

@SuppressWarnings("unused")
public class LoginJSInterface {
    private Context mContext;

    public LoginJSInterface(Context context){
        mContext = context;
    }

    @JavascriptInterface
    public void setUserName(String username){
        if(!username.isEmpty()) ((LoginActivity)mContext).hasLoggedIn(username);
    }

    @JavascriptInterface
    public void showError(boolean hasError, final String details){
        if (!hasError) return;
        ((LoginActivity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((LoginActivity)mContext).showLoginError(details);
            }
        });
    }
}

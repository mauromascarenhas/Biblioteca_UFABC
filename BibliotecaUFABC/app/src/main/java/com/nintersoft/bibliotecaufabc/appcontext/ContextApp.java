package com.nintersoft.bibliotecaufabc.appcontext;

import android.annotation.SuppressLint;
import android.app.Application;

@SuppressLint("Registered")
public class ContextApp extends Application {
    private static ContextApp context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static ContextApp getContext(){
        return context;
    }
}

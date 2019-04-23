package com.nintersoft.bibliotecaufabc.appcontext;

import android.app.Application;

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

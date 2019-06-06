package com.nintersoft.bibliotecaufabc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebView;

import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.webviewclients.SyncWebClient;

@SuppressWarnings("FieldCanBeLocal")
public class SyncActivity extends AppCompatActivity {

    private final String LOG_TAG = "ACTIVITY_SYNC";
    private final String LOG_RESUMED = "RESUMED";
    private final String LOG_CREATED = "CREATED";
    private final String LOG_STOPPED = "STOPPED";
    private final String LOG_DESTROYED = "DESTROYED";

    private WebView dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SyncActivity.this.finish();
            }
        }, 10000);

        Log.v(LOG_TAG, LOG_CREATED);

        bindComponents();
    }

    private void bindComponents(){
        dataSource = new WebView(this);
        GlobalFunctions.configureStandardWebView(dataSource);
        dataSource.setWebViewClient(new SyncWebClient(this));
        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RENEWAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, LOG_RESUMED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, LOG_STOPPED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, LOG_DESTROYED);
    }
}

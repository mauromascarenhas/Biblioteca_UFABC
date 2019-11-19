package com.nintersoft.bibliotecaufabc.synchronization;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.webviewclients.SyncWebClient;

import org.json.JSONException;
import org.json.JSONObject;

public class SyncService extends Service {

    private WebView dataSource;
    private WindowManager windowManager;
    private boolean isScheduled;

    private Handler mHandler;
    private Runnable killService;
    private Runnable errorChecker;

    public SyncService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        GlobalFunctions.createSyncNotificationChannel(this);
        GlobalFunctions.createRenewalNotificationChannel(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                GlobalFunctions.createSyncNotification(this,
                        R.string.notification_sync_removed_title,
                        R.string.notification_sync_removed_message,
                        GlobalConstants.SYNC_NOTIFICATION_REVOKED_ID);
                GlobalFunctions.cancelPeriodicSync(getApplicationContext());
                return;
            }
        }

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            params = new WindowManager.LayoutParams(
                   WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                   WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                   WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                   PixelFormat.TRANSLUCENT);
        else params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.width = params.height = 0;
        params.x = params.y = 0;

        dataSource = new WebView(this);
        GlobalFunctions.configureStandardWebView(dataSource);
        dataSource.setWebViewClient(new SyncWebClient(this));

        windowManager.addView(dataSource, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (dataSource != null) {
            isScheduled = intent.getBooleanExtra(GlobalConstants.SYNC_INTENT_SCHEDULED, true);
            startForeground(GlobalConstants.SYNC_NOTIFICATION_ID, createSyncingNotification());
            dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RENEWAL);
            return super.onStartCommand(intent, flags, startId);
        }
        else{
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (dataSource != null) windowManager.removeView(dataSource);
        if (mHandler != null) mHandler.removeCallbacks(killService);
    }

    public void killLater(){
        killService = new Runnable() {
            @Override
            public void run() {
                SyncService.this.retryAndFinish();
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(killService, 180000);
    }

    public void scheduleChecking(){
        errorChecker = new Runnable() {
            @Override
            public void run() {
                if (dataSource.getUrl().contains(GlobalConstants.URL_LIBRARY_LOGIN_P))
                    checkForErrors();
            }
        };
        mHandler.postDelayed(errorChecker, 1000);
    }

    public void checkForErrors(){
        String script = String.format("%1$s \ncheckForErrors();",
                GlobalFunctions.getScriptFromAssets(getApplicationContext(), "javascript/login_scraper.js"));
        dataSource.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(final String value) {
                try {
                    JSONObject result = new JSONObject(value);
                    if (result.getBoolean("hasFormError")) {
                        GlobalFunctions.createSyncNotification(getApplicationContext(),
                                R.string.notification_sync_error_title,
                                R.string.notification_sync_error_message,
                                GlobalConstants.SYNC_NOTIFICATION_UPDATE_ID);
                        SyncService.this.finish();
                    }
                    else mHandler.postDelayed(errorChecker, 250);
                } catch (JSONException e){
                    SyncService.this.retryAndFinish();
                }
            }
        });
    }

    private Notification createSyncingNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, GlobalConstants.CHANNEL_SYNC_ID);
        return builder.setSmallIcon(R.drawable.ic_default_book)
                .setContentTitle(this.getString(R.string.notification_syncing_title))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
                .setContentText(this.getString(R.string.notification_syncing_message))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(this.getString(R.string.notification_syncing_message)))
                .setProgress(0, 0, true)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
    }

    public void retryAndFinish(){
        if (isScheduled) GlobalFunctions.scheduleRetrySync(getApplicationContext());
        finish();
    }

    public void finish(){
        stopForeground(true);
        stopSelf();
    }
}

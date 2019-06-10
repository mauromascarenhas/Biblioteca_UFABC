package com.nintersoft.bibliotecaufabc.synchronization;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.webviewclients.SyncWebClient;

public class SyncService extends Service {

    private WebView dataSource;
    private WindowManager windowManager;

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
                stopSelf();
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
        startForeground(GlobalConstants.SYNC_NOTIFICATION_ID, createSyncingNotification());
        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RENEWAL);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v("Service", "DESTROY");

        if (dataSource != null) windowManager.removeView(dataSource);
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

    public void finish(){
        stopForeground(true);
        stopSelf();
    }
}

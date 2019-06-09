package com.nintersoft.bibliotecaufabc.synchronization;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.nintersoft.bibliotecaufabc.MainActivity;
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
                requestUpdateNotification();
                stopSelf();
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

    private void requestUpdateNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, GlobalConstants.CHANNEL_SYNC_ID);
        builder.setSmallIcon(R.drawable.ic_default_book)
                .setContentTitle(this.getString(R.string.notification_sync_removed_title))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] {750, 750})
                .setColor(ContextCompat.getColor(this, android.R.color.holo_purple))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(this.getString(R.string.notification_sync_removed_message))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(this.getString(R.string.notification_sync_removed_message)))
                .setAutoCancel(true);

        Intent renewalActivity = new Intent(this, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(this, GlobalConstants.ACTIVITY_RENEWAL_REQUEST_CODE,
                renewalActivity, 0);
        builder.setContentIntent(activity);

        int notificationID = GlobalConstants.SYNC_REQUEST_INTENT_ID;
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(notificationID, builder.build());
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
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
    }
}

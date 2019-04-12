package com.nintersoft.bibliotecaufabc.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nintersoft.bibliotecaufabc.constants.GlobalConstants;

public class NotificationDisplay extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(GlobalConstants.NOTIFICATION_CONTENT);
        int notificationID = intent.getIntExtra(GlobalConstants.NOTIFICATION_ID, -1);
        if (notificationManager != null)
            notificationManager.notify(notificationID, notification);
    }
}

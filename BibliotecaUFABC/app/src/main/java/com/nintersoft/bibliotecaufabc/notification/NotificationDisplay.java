package com.nintersoft.bibliotecaufabc.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

public class NotificationDisplay extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationID = intent.getIntExtra(GlobalConstants.NOTIFICATION_ID, -1);
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(notificationID,
                    GlobalFunctions.createNotification(context, notificationID, intent.getStringExtra(GlobalConstants.NOTIFICATION_MESSAGE)));
    }
}

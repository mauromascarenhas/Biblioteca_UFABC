package com.nintersoft.bibliotecaufabc.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.R;

public class SyncNotificationDisplay extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GlobalFunctions.createSyncNotification(context,
                R.string.notification_sync_title,
                R.string.notification_sync_message,
                intent.getIntExtra(GlobalConstants.NOTIFICATION_ID, -1));
    }
}

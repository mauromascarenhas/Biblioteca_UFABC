package com.nintersoft.bibliotecaufabc.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

import com.nintersoft.bibliotecaufabc.synchronization.SyncService;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.R;

public class SyncNotificationDisplay extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long nextAlarm = System.currentTimeMillis()
                - preferences.getLong(context.getString(R.string.key_synchronization_schedule), 0);
        if (nextAlarm > 432000000) {
            GlobalFunctions.createSyncNotification(context,
                    R.string.notification_sync_title,
                    R.string.notification_sync_message,
                    intent.getIntExtra(GlobalConstants.NOTIFICATION_ID, -1));
            ContextCompat.startForegroundService(context, new Intent(context, SyncService.class));
        }
        else GlobalFunctions.scheduleSyncNotification(context, nextAlarm);
    }
}

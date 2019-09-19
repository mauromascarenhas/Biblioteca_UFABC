package com.nintersoft.bibliotecaufabc.notification;

import android.app.AlarmManager;
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        long nextAlarm = System.currentTimeMillis()
                - preferences.getLong(context.getString(R.string.key_synchronization_schedule), 0);
        if (nextAlarm > AlarmManager.INTERVAL_DAY * GlobalConstants.SYNC_REMINDER_NOTIFICATION_INTERVAL) {
            GlobalFunctions.createSyncNotification(context,
                    R.string.notification_sync_title,
                    R.string.notification_sync_message,
                    intent.getIntExtra(GlobalConstants.NOTIFICATION_ID, GlobalConstants.SYNC_NOTIFICATION_REMINDER_ID));
            ContextCompat.startForegroundService(context, new Intent(context, SyncService.class));
        }
    }
}

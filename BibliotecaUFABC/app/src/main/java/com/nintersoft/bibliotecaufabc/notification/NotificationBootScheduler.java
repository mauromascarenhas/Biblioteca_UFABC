package com.nintersoft.bibliotecaufabc.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDatabaseSingletonFactory;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

public class NotificationBootScheduler extends BroadcastReceiver {

    // TODO: Remove when complete!

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equalsIgnoreCase(intent.getAction())){
            //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            //long lastSyncAgo = System.currentTimeMillis() - preferences.getLong(context.getString(R.string.key_synchronization_schedule), 0),
                //syncInterval = preferences.getLong(context.getString(R.string.key_notification_sync_interval), 2) * 86400000;

            BookRenewalDAO dao = BookRenewalDatabaseSingletonFactory.getInstance().bookRenewalDAO();

            GlobalFunctions.createSyncNotificationChannel(context.getApplicationContext());
            GlobalFunctions.createRenewalNotificationChannel(context.getApplicationContext());
            GlobalFunctions.scheduleSyncNotification(context, -1);
            GlobalFunctions.scheduleRenewalAlarms(context, dao);
            // GlobalFunctions.scheduleNextSynchronization(context, lastSyncAgo > syncInterval ? 1000 : GlobalFunctions.nextStandardSync(1));
        }
    }
}

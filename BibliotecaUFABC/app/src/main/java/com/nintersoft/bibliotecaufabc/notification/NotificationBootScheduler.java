package com.nintersoft.bibliotecaufabc.notification;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDatabaseSingletonFactory;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

public class NotificationBootScheduler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equalsIgnoreCase(intent.getAction())){

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            BookRenewalDAO dao = BookRenewalDatabaseSingletonFactory.getInstance().bookRenewalDAO();
            String syncInterval = preferences.getString(context.getString(R.string.key_synchronization_schedule), "2");
            if (syncInterval == null) syncInterval = "2";

            GlobalFunctions.createSyncNotificationChannel(context.getApplicationContext());
            GlobalFunctions.createRenewalNotificationChannel(context.getApplicationContext());
            GlobalFunctions.schedulePeriodicSyncReminder(context.getApplicationContext(),
                    -1, AlarmManager.INTERVAL_DAY * GlobalConstants.SYNC_REMINDER_NOTIFICATION_INTERVAL);
            GlobalFunctions.scheduleRenewalAlarms(context, dao);
            GlobalFunctions.schedulePeriodicSync(context.getApplicationContext(), AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    Integer.parseInt(syncInterval) * AlarmManager.INTERVAL_DAY);
        }
    }
}

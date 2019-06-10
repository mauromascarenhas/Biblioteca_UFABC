package com.nintersoft.bibliotecaufabc.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDatabaseSingletonFactory;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;

public class NotificationBootScheduler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equalsIgnoreCase(intent.getAction())){
            BookRenewalDAO dao = BookRenewalDatabaseSingletonFactory.getInstance().bookRenewalDAO();

            //TODO: Fix this old fashioned notifications
            GlobalFunctions.createSyncNotificationChannel(context.getApplicationContext());
            GlobalFunctions.createRenewalNotificationChannel(context.getApplicationContext());
            GlobalFunctions.scheduleSyncNotification(context, -1);
            GlobalFunctions.scheduleRenewalAlarms(context, dao);
        }
    }
}

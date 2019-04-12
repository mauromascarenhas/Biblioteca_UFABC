package com.nintersoft.bibliotecaufabc.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalContract;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDatabase;
import com.nintersoft.bibliotecaufabc.constants.GlobalConstants;

import androidx.room.Room;

public class NotificationBootScheduler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equalsIgnoreCase(intent.getAction())){
            BookRenewalDAO dao = Room.databaseBuilder(context, BookRenewalDatabase.class,
                    BookRenewalContract.DB_NAME).allowMainThreadQueries().build().bookRenewalDAO();

            GlobalConstants.createNotificationChannel(context.getApplicationContext());
            GlobalConstants.scheduleRenewalAlarms(context, dao);
            // For DEBUG purposes
            GlobalConstants.scheduleBookNotification(context, 5000, 50, null);
        }
    }
}

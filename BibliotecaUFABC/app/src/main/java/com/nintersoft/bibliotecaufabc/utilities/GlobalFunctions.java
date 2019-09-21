package com.nintersoft.bibliotecaufabc.utilities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.activities.RenewalActivity;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalProperties;
import com.nintersoft.bibliotecaufabc.notification.NotificationDisplay;
import com.nintersoft.bibliotecaufabc.notification.SyncNotificationDisplay;
import com.nintersoft.bibliotecaufabc.synchronization.SyncExecutioner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Methods which are useful in different parts of the application
 */
public class GlobalFunctions {
    /**
     * Configures the given #WebView with the application standard settings,
     * allowing it to run JavaScript content and keep cache
     * @param v : WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    public static void configureStandardWebView(@NonNull WebView v){
        WebSettings webSettings = v.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
    }

    /**
     * Retrieves the asset content as a text, using the #context to get the specified asset
     *
     * @param context  : Context used for data building and retrieval
     * @param filePath : Relative path to the desired asset (must be inside "assets" folder)
     * @return         : Returns the file content as a string or null if an exception has occurred
     */
    public static String getScriptFromAssets(Context context, String filePath) {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = context.getResources().getAssets().open(filePath);

            BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            String str;
            while ((str = br.readLine()) != null) sb.append(str).append("\n");
            br.close();
            return  sb.toString();
        } catch (Exception e){
            return null;
        }
    }

    /**
     * Schedules a recurrent synchronization reminder within the given initial
     * delay and periodic interval (which must be in milliseconds)
     * Take notice that this method also removes previously scheduled requests
     *
     * @param context :  Context used for data building and retrieval
     * @param initialDelay     : Time in milliseconds to trigger the first synchronization operation
     * @param periodicInterval : Periodic interval in which the task must be executed (milliseconds)
     */
    public static void schedulePeriodicSyncReminder(Context context, long initialDelay, long periodicInterval){
        if (initialDelay < 0) initialDelay = 1000;

        Intent notificationIntent = new Intent(context, SyncNotificationDisplay.class)
                .putExtra(GlobalConstants.NOTIFICATION_ID, GlobalConstants.SYNC_NOTIFICATION_REMINDER_ID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.SYNC_NOTIFICATION_REMINDER_ID, notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, initialDelay,
                    periodicInterval, pendingIntent);
        }
    }

    /**
     * Schedules a renewal notification/warning with the given Id and time (which must be in milliseconds)
     * Help from : https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
     *
     * @param context        : Context used for data building and retrieval
     * @param delay          : Time in milliseconds to trigger the notification exhibition
     * @param notificationId : Unique identifier for the new notification (may be replaced if there is
     *                          another with the same ID)
     * @param message        : Message to be displayed
     */
    @SuppressWarnings("WeakerAccess")
    public static void scheduleBookNotification(Context context, long delay,
                                                int notificationId, @Nullable String message){
        Intent notificationIntent = new Intent(context, NotificationDisplay.class);
        notificationIntent.putExtra(GlobalConstants.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(GlobalConstants.NOTIFICATION_MESSAGE, message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + delay, pendingIntent);
            else alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + delay, pendingIntent);
        }
    }

    /**
     * Creates a generic notification with a pending intent to RenewalActivity.
     * @param context     : Context used for data building and retrieval
     * @param title_rId   : Id of the title string resource
     * @param message_rId : Id of the message string resource
     * @return            : Returns the notification already built
     */
    private static Notification createSyncNotification(Context context, int title_rId, int message_rId){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GlobalConstants.CHANNEL_SYNC_ID);
        builder.setSmallIcon(R.drawable.ic_default_book)
                .setContentTitle(context.getString(title_rId))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] {750, 750})
                .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(context.getString(message_rId))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getString(message_rId)))
                .setAutoCancel(true);

        Intent renewalActivity = new Intent(context, RenewalActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, GlobalConstants.ACTIVITY_RENEWAL_REQUEST_CODE,
                renewalActivity, 0);
        builder.setContentIntent(activity);

        return builder.build();
    }

    /**
     * Creates a notification with a pending intent to RenewalActivity and post it.
     * @param context     : Context used for data building and retrieval
     * @param title_rId   : Id of the title string resource
     * @param message_rId : Id of the message string resource
     * @param id      : Notification id
     */
    public static void createSyncNotification(Context context, int title_rId, int message_rId, int id){
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(id, GlobalFunctions.createSyncNotification(context, title_rId, message_rId));
    }

    /**
     * Creates a renewal notification with the given message.
     * If no message is passed to the method, it will build a generic one to be displayed
     * @param context : Context used for data building and retrieval
     * @param message : Message which will be displayed in the notification body
     * @return        : Returns the notification already built
     */
    public static Notification createRenewalNotification(Context context, @Nullable String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GlobalConstants.CHANNEL_RENEWAL_ID);
        builder.setSmallIcon(R.drawable.ic_default_book)
                .setContentTitle(context.getString(R.string.notification_book_renewal_title))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(new long[] {750, 750})
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);
        if (message == null)
            builder.setContentText(context.getString(R.string.notification_book_renewal_content))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(context.getString(R.string.notification_book_renewal_content)));
        else builder.setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message));

        Intent renewalActivity = new Intent(context, RenewalActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, GlobalConstants.ACTIVITY_RENEWAL_REQUEST_CODE,
                                                            renewalActivity, 0);
        builder.setContentIntent(activity);

        return builder.build();
    }

    /**
     * This method cancels the scheduling of the notification which has the given Id.
     *
     * @param context        : Context used for building intent building and comparison
     * @param bookId         : Id of the notification to be cancelled
     */
    @SuppressWarnings("WeakerAccess")
    public static void cancelScheduledNotification(Context context, long bookId){
        Intent notificationIntent = new Intent(context, NotificationDisplay.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            for (int i = 0; i < 3; ++i)
                alarmManager.cancel(PendingIntent.getBroadcast(context,
                        (int)((bookId + (i * 500)) % (Integer.MAX_VALUE + 1L)),
                        notificationIntent, 0));
    }

    /**
     * Creates notification channel for synchronization requests
     * @param context : Used for getting string messages
     */
    public static void createSyncNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_renewal_channel_title);
            String description = context.getString(R.string.notification_renewal_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(GlobalConstants.CHANNEL_SYNC_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Creates notification channel for book renewal
     *
     * @param context : Used for getting string messages
     */
    public static void createRenewalNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_renewal_channel_title);
            String description = context.getString(R.string.notification_renewal_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(GlobalConstants.CHANNEL_RENEWAL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Cancels every existing pending renewal warning
     *
     * @param context : Used for intents comparison
     * @param dao     : Used for getting previous book details
     */
    public static void cancelExistingScheduledAlarms(Context context, BookRenewalDAO dao){
        List<BookRenewalProperties> oldData = dao.getAll();
        for (BookRenewalProperties b : oldData)
            GlobalFunctions.cancelScheduledNotification(context.getApplicationContext(), b.getId());
    }

    /**
     * Schedules notification alarms for every #BookRenewalProperties available at DAO
     * @param context : Context used for building intent building
     * @param dao     : Data Access Object which gives access to the stored #BookRenewalProperties
     */
    public static void scheduleRenewalAlarms(Context context, BookRenewalDAO dao){
        List<BookRenewalProperties> availableBooks = dao.getAll();

        if (GlobalVariables.ringAlarm) {
            HashMap<String, ArrayList<BookRenewalProperties>> groupedBooks = new HashMap<>();

            // Groups books by date
            for (BookRenewalProperties brp : availableBooks){
                ArrayList<BookRenewalProperties> brps = groupedBooks.get(brp.getDate());

                if (brps == null){
                    brps = new ArrayList<>();
                    groupedBooks.put(brp.getDate(), brps);
                }
                brps.add(brp);
            }

            // Performs schedules
            for (String d : groupedBooks.keySet()){
                ArrayList<BookRenewalProperties> brps = groupedBooks.get(d);
                if (brps == null || brps.isEmpty()) return;

                BookRenewalProperties min = brps.get(0);
                for (int i = 1; i < brps.size(); ++i)
                    if (min.getId() > brps.get(i).getId()) min = brps.get(i);

                String nMessage;
                if (brps.size() == 1)
                    nMessage = context.getString(R.string.notification_book_renewal_specific_content, brps.get(0).getTitle());
                else {
                    StringBuilder bNames = new StringBuilder();
                    bNames.append("- ").append(brps.get(0).getTitle()).append(";");
                    for (int i = 1; i < brps.size(); ++i)
                        bNames.append("\n- ")
                                .append(brps.get(i).getTitle())
                                .append(";");
                    nMessage = context.getString(R.string.notification_book_renewal_specific_content_multiple, bNames.toString());
                }

                Matcher m = Pattern.compile("(\\d{2}/\\d{2}/\\d{2})", Pattern.CASE_INSENSITIVE)
                        .matcher(min.getDate());

                if (m.find()){
                    try {
                        for (int k = 0; k < 3; ++k) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new SimpleDateFormat("dd/MM/yy HH:mm:ss",
                                    new Locale("pt", "BR"))
                                    .parse(m.group(1) + String.format(new Locale("pt", "BR"),
                                            " %02d:00:00", 8 + (5 * k))));
                            calendar.add(Calendar.DAY_OF_MONTH, GlobalVariables.ringAlarmOffset);

                            long millis = calendar.getTime().getTime() - (new Date()).getTime();
                            if (millis > 0) GlobalFunctions.scheduleBookNotification(context.getApplicationContext(), millis,
                                    (int) ((min.getId() + (500 * k)) % (Integer.MAX_VALUE + 1L)), nMessage);
                        }
                    } catch (ParseException e) {
                        Toast.makeText(context, context.getString(R.string.snack_message_parse_fail), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    /**
     * Schedules a recurrent synchronization procedure within the given initial
     * delay and periodic interval (which must be in milliseconds)
     * Take notice that this method also removes previously scheduled requests
     *
     * @param context          : Context used for data building and retrieval
     * @param initialDelay     : Time in milliseconds to trigger the first synchronization operation
     * @param periodicInterval : Periodic interval in which the task must be executed (milliseconds)
     */
    public static void schedulePeriodicSync(Context context, long initialDelay, long periodicInterval){
        Intent notificationIntent = new Intent(context, SyncExecutioner.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.SYNC_EXECUTIONER_INTENT_ID,
                notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + initialDelay, periodicInterval,
                    pendingIntent);
        }
    }

    /**
     * Cancels the recurrent synchronization scheduled with
     * #schedulePeriodicSync(Context, long, long)
     *
     * @param context          : Context used for data building and retrieval
     */
    public static void cancelPeriodicSync(Context context){
        Intent notificationIntent = new Intent(context, SyncExecutioner.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.SYNC_EXECUTIONER_INTENT_ID,
                notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.cancel(pendingIntent);
    }

    /**
     * Schedules a new synchronization procedure with 15 minutes of delay.
     * If a recurrent sync is already scheduled, then it is ignored (nothing is done)
     * Take notice that this method also removes previously scheduled requests
     *
     * @param context        : Context used for data building and retrieval
     */
    public static void scheduleRetrySync(Context context){
        // Do not sync if matches with a scheduled one
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String syncInterval = prefs.getString(context.getString(R.string.key_notification_sync_interval), "2");
        if (prefs.getLong(context.getString(R.string.key_synchronization_schedule), System.currentTimeMillis() - 1)
                + (Integer.parseInt(syncInterval == null ? "2" : syncInterval) * AlarmManager.INTERVAL_DAY)
                - System.currentTimeMillis() <= AlarmManager.INTERVAL_FIFTEEN_MINUTES)
            return;

        Intent notificationIntent = new Intent(context, SyncExecutioner.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.SYNC_EXECUTIONER_INTENT_RETRY_ID,
                notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
            else alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
    }
}

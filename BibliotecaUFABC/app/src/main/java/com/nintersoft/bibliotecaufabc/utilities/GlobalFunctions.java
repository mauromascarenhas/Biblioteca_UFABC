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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.RenewalActivity;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalProperties;
import com.nintersoft.bibliotecaufabc.notification.NotificationDisplay;
import com.nintersoft.bibliotecaufabc.notification.SyncNotificationDisplay;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
     * Executes the given script in the given #WebView, maintaining support for
     * every SDKs version
     * @param v  : WebView
     * @param js : JavaScript content
     */
    public static void executeScript(WebView v, String js){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            v.evaluateJavascript(js, null);
        else v.loadUrl(js);
    }

    /**
     * Retrieves the asset content as a text, using the #context to get the specified asset
     *
     * @param context  : Context used for data building and retrieval
     * @param filePath : Relative path to the desired assed (must be inside "assets" folder)
     * @return         : Returns the file content as a string or null if an exeption has occurred
     */
    @SuppressWarnings("all")
    public static String getScriptFromAssets(Context context, String filePath) {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = context.getResources().getAssets().open(filePath);

            BufferedReader br;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            else br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String str;
            while ((str = br.readLine()) != null) sb.append(str).append("\n");
            br.close();
            return  sb.toString();
        } catch (Exception e){
            return null;
        }
    }

    /**
     * Schedules a synchronization warning/notification for the current time plus the
     * given delay (in milliseconds). It is important to notice that it cancels any previous
     * notifications and then schedules a new one. Besides doing that, it will store the scheduling
     * as a SharedPreferences key so as to retrieve it in case of reboot.
     *
     * @param context :  Context used for data building and retrieval
     * @param delay   :  Time in milliseconds to trigger the notification exhibition
     *                   if the delay is negative, then it try to get the last scheduled sync request time.
     *                   Useful for rescheduling on boot
     */
    public static void scheduleSyncNotification(Context context, long delay){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (delay < 0){
            long currentTime = SystemClock.elapsedRealtime();
            long lastSchedule = preferences.getLong(context.getString(R.string.key_synchronization_schedule), currentTime - 1);
            delay = (lastSchedule < currentTime) ? 1000 : lastSchedule - currentTime;
        }

        Intent notificationIntent = new Intent(context, SyncNotificationDisplay.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.SYNC_NOTIFICATION_ID, notificationIntent, 0);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(context.getString(R.string.key_synchronization_schedule), futureInMillis);
            editor.apply();
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

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    /**
     * Creates a synchronization request notification.
     * @param context : Context used for data building and retrieval
     * @return        : Returns the notification already built
     */
    public static Notification createSyncNotification(Context context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GlobalConstants.CHANNEL_SYNC_ID);
        builder.setSmallIcon(R.drawable.ic_default_book)
                .setContentTitle(context.getString(R.string.notification_sync_title))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] {750, 750})
                .setColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(context.getString(R.string.notification_sync_message))
                .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(context.getString(R.string.notification_sync_message)))
                .setAutoCancel(true);

        Intent renewalActivity = new Intent(context, RenewalActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, GlobalConstants.ACTIVITY_RENEWAL_REQUEST_CODE,
                renewalActivity, 0);
        builder.setContentIntent(activity);

        return builder.build();
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
     * @param context        : Context used for building intent building and comparison
     * @param notificationId : Id of the notification to be cancelled
     */
    @SuppressWarnings("WeakerAccess")
    public static void cancelScheduledNotification(Context context, int notificationId){
        Intent notificationIntent = new Intent(context, NotificationDisplay.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            for (int i = 0; i < 3; ++i){
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId + (i * 500),
                        notificationIntent, 0);
                alarmManager.cancel(pendingIntent);
            }
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
     * @param context : Used for intents comparison
     * @param dao     : Used for getting previous book details
     */
    public static void cancelExistingScheduledAlarms(Context context, BookRenewalDAO dao){
        List<BookRenewalProperties> oldData = dao.getAll();
        for (BookRenewalProperties b : oldData)
            GlobalFunctions.cancelScheduledNotification(context, (int)b.getId());
    }

    /**
     * Schedules notification alarms for every #BookRenewalProperties available at DAO
     * @param context : Context used for building intent building
     * @param dao     : Data Access Object which gives access to the stored #BookRenewalProperties
     */
    public static void scheduleRenewalAlarms(Context context, BookRenewalDAO dao){
        List<BookRenewalProperties> availableBooks = dao.getAll();

        if (GlobalConstants.ringAlarm) {
            for (int i = availableBooks.size() - 1; i > -1; --i) {
                BookRenewalProperties b = availableBooks.get(i);
                long id = b.getId();

                Matcher m = Pattern.compile("(\\d{2}/\\d{2}/\\d{2})", Pattern.CASE_INSENSITIVE)
                        .matcher(b.getDate());

                if (m.find()){
                    try {
                        for (int k = 0; k < 3; ++k) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new SimpleDateFormat("dd/MM/yy HH:mm:ss",
                                    new Locale("pt", "BR"))
                                    .parse(m.group(1) + String.format(new Locale("pt", "BR"),
                                                                        " %02d:%02d:00", 8 + (5 * k), i)));
                            calendar.add(Calendar.DAY_OF_MONTH, GlobalConstants.ringAlarmOffset);

                            long millis = calendar.getTime().getTime() - (new Date()).getTime();
                            if (millis > 0) GlobalFunctions.scheduleBookNotification(context.getApplicationContext(), millis,
                                    (int) id + (500 * k), context.getString(R.string.notification_book_renewal_specific_content, b.getTitle()));
                        }
                    } catch (ParseException e) {
                        Toast.makeText(context, context.getString(R.string.snack_message_parse_fail), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }
}

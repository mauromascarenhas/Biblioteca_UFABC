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
        if (delay < 0) delay = 1000;

        Intent notificationIntent = new Intent(context, SyncNotificationDisplay.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.SYNC_NOTIFICATION_ID, notificationIntent, 0);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
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

        if (GlobalVariables.ringAlarm) {
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
                            calendar.add(Calendar.DAY_OF_MONTH, GlobalVariables.ringAlarmOffset);

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

    // TODO: Remove if pass tests
    /**
     * Schedules a new synchronization procedure within the given delay (which must be in milliseconds)
     * Take notice that this method also removes previously scheduled requests
     *
     * @deprecated WorkManager has replaced all of those method calls
     *
     * @param context        : Context used for data building and retrieval
     * @param delay          : Time in milliseconds to trigger the synchronization operation
     */
    @Deprecated
    public static void scheduleNextSynchronization(Context context, long delay){
        //_DEBUG: Remove function call and scope
        Date current = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String dateAsString = df.format(new Date(current.getTime() + delay));
        GlobalFunctions.writeToFile(dateAsString, "schedule");

        Intent notificationIntent = new Intent(context, SyncExecutioner.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.SYNC_EXECUTIONER_INTENT_ID,
                notificationIntent, 0);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        }
    }

    // TODO: Remove if pass tests
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
        //_DEBUG: Remove function call and scope
        Date current = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String dateAsString = df.format(new Date(current.getTime() + initialDelay))
                + " . Interval " + periodicInterval;
        GlobalFunctions.writeToFile(dateAsString, "periodic_schedule");

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

    // TODO: Remove if pass tests
    /**
     * Schedules a new synchronization procedure with 15 minutes of delay.
     * If a recurrent sync is already scheduled, then it is ignored (nothing is done)
     * Take notice that this method also removes previously scheduled requests
     *
     * @param context        : Context used for data building and retrieval
     */
    public static void scheduleRetrySync(Context context){
        //_DEBUG: Remove function call and scope
        Date current = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String dateAsString = df.format(new Date(current.getTime() + 900000));
        GlobalFunctions.writeToFile(dateAsString, "retry_schedule");

        // Do not sync if matches with a scheduled one
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getLong(context.getString(R.string.key_synchronization_schedule), 0)
                + (prefs.getLong(context.getString(R.string.key_notification_sync_interval), 2)
                    * 86400000L) - SystemClock.elapsedRealtime() + 900000 <= 0)
            return;

        Intent notificationIntent = new Intent(context, SyncExecutioner.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.SYNC_EXECUTIONER_INTENT_RETRY_ID,
                notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 900000, pendingIntent);
            else alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 900000, pendingIntent);
        }
    }

    /**
     * Convenience function to get the next standard sync delay
     *
     * @deprecated WorkManager has replaced all of those method calls
     *
     * @return : The next standard synchronization time (delay in millis)
     */
    @Deprecated
    public static long nextStandardSync(){
        return (82800000 - System.currentTimeMillis() % 86400000) + (86400000 * GlobalVariables.syncInterval);
    }

    /**
     * Convenience function to get the next standard sync delay
     *
     * @deprecated WorkManager has replaced all of those method calls
     *
     * @param maxDays : Used to reference the next sync (in days) instead of the global standard
     * @return        : The next standard synchronization time (delay in millis)
     */
    @Deprecated
    public static long nextStandardSync(int maxDays){
        return (82800000 - System.currentTimeMillis() % 86400000) + (86400000 * maxDays);
    }

    //_DEBUG: Remove function declaration
    public static void writeToFile(String data, String appendName) {
        try {
            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/BIBLIOTECA_UFABC/");

            if(!path.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                path.mkdirs();
            }

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
            String dateAsString = df.format(new Date());

            File file = new File(path, String.format("%2$s_%1$s.txt", appendName, dateAsString));
            Log.v("File write", path + "/" + String.format("%1$s_%2$s.txt", appendName, dateAsString));

            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

//    _DEBUG: Remove function declaration
//    public static void writeToFile(String data, String appendName, Context context) {
//        try {
//            DateFormat df = new SimpleDateFormat("yyyy-mm-dd_hh-mm-ss", Locale.getDefault());
//            String dateAsString = df.format(new Date());
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(String.format("%1$s_%2$s.txt", appendName, dateAsString), Context.MODE_PRIVATE));
//            outputStreamWriter.write(data);
//            outputStreamWriter.close();
//        }
//        catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }
//    }
}

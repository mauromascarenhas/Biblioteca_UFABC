package com.nintersoft.bibliotecaufabc.utilities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.SystemClock;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.nintersoft.bibliotecaufabc.MainActivity;
import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalProperties;
import com.nintersoft.bibliotecaufabc.notification.NotificationDisplay;

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

public class GlobalFunctions {
    @SuppressLint("SetJavaScriptEnabled")
    public static void configureStandardWebView(@NonNull WebView v){
        WebSettings webSettings = v.getSettings();
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
    }

    public static void executeScript(WebView v, String js){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            v.evaluateJavascript(js, null);
        else v.loadUrl(js);
    }

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

    /*
     * Help from : https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
     */

    public static void scheduleBookNotification(Context context, long delay,
                                                int notificationId, @Nullable String message){
        Intent notificationIntent = new Intent(context, NotificationDisplay.class);
        notificationIntent.putExtra(GlobalConstants.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(GlobalConstants.NOTIFICATION_MESSAGE, message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    public static Notification createNotification(Context context, int notificationId, @Nullable String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, GlobalConstants.CHANNEL_ID);
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

        Intent homeActivity = new Intent(context, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, notificationId, homeActivity, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(activity);

        return builder.build();
    }

    @SuppressWarnings("WeakerAccess")
    public static void cancelScheduledNotification(Context context, int notificationId){
        Intent notificationIntent = new Intent(context, NotificationDisplay.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) alarmManager.cancel(pendingIntent);
    }

    public static void createNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_title);
            String description = context.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(GlobalConstants.CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    public static void cancelExistingScheduledAlarms(Context context, BookRenewalDAO dao){
        List<BookRenewalProperties> oldData = dao.getAll();
        for (BookRenewalProperties b : oldData)
            GlobalFunctions.cancelScheduledNotification(context, (int)b.getId());
    }

    public static void scheduleRenewalAlarms(Context context, BookRenewalDAO dao){
        List<BookRenewalProperties> availableBooks = dao.getAll();

        if (GlobalConstants.ringAlarm) {
            for (BookRenewalProperties b : availableBooks) {
                long id = b.getId();

                Matcher m = Pattern.compile("(\\d{2}/\\d{2}/\\d{2})", Pattern.CASE_INSENSITIVE)
                        .matcher(b.getDate());

                if (m.find()){
                    try {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new SimpleDateFormat("dd/MM/yy HH:mm:ss",
                                new Locale("pt", "BR"))
                                .parse(m.group(1) + " 08:00:00"));
                        calendar.add(Calendar.DAY_OF_MONTH, GlobalConstants.ringAlarmOffset);

                        long millis = calendar.getTime().getTime() - (new Date()).getTime();
                        // TODO : Remove item?
                        if (millis < 0) continue;
                        GlobalFunctions.scheduleBookNotification(context.getApplicationContext(), millis,
                                (int)id, context.getString(R.string.notification_book_renewal_specific_content, b.getTitle()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

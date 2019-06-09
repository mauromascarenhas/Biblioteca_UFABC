package com.nintersoft.bibliotecaufabc.webviewclients;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.RenewalActivity;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDatabaseSingletonFactory;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalProperties;
import com.nintersoft.bibliotecaufabc.synchronization.SyncService;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.utilities.GlobalVariables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SyncWebClient extends WebViewClient {
    private int login_page_finished;
    private int login_home_finished;
    private int renewal_page_finished;

    private String user_login;
    private String user_password;

    private Context mContext;

    public SyncWebClient(Context context){
        super();
        mContext = context;

        login_page_finished = 0;
        login_home_finished = 0;
        renewal_page_finished = 0;

        loadSettings();

        Log.v("AQUI", "AQUI");
    }

    private void loadSettings(){
        //TODO: Improve sync settings -> Implement?
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        GlobalVariables.storeUserFormData = pref.getBoolean(mContext.getString(R.string.key_privacy_store_password), true);

        user_login = pref.getString(mContext.getString(R.string.key_privacy_login_username), "");
        user_password = pref.getString(mContext.getString(R.string.key_privacy_login_password), "");

        if (user_login == null || user_password == null){
            GlobalFunctions.scheduleNextSynchronization(mContext, 86400000);
            ((SyncService)mContext).stopSelf();
        }
        if (!GlobalVariables.storeUserFormData || user_login.isEmpty()
                || user_password.isEmpty()){
            GlobalFunctions.scheduleNextSynchronization(mContext, 86400000);
            ((SyncService)mContext).stopSelf();
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

        Log.v("Lendo...", url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public void onPageFinished(final WebView view, String url) {
        super.onPageFinished(view, url);

        Log.v("Terminou!", url);

        //TODO: Check this method
        if (url.contains(GlobalConstants.URL_ACCESS_PAGE)){
            if (login_page_finished == 0){
                String script = String.format("%1$s \nperformLogin(\"%2$s\",\"%3$s\");",
                        GlobalFunctions.getScriptFromAssets(mContext, "javascript/login_scraper.js"),
                        user_login, user_password);
                view.evaluateJavascript(script, null);
                login_page_finished++;
            }

            String script = String.format("%1$s \ncheckForErrors();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/login_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    try {
                        JSONObject result = new JSONObject(value);
                        if (result.getBoolean("hasFormError")) {
                            requestUpdateNotification();
                            //TODO: Schedule for the next hour or do another thing
                            ((SyncService)mContext).stopSelf();
                        }
                    } catch (JSONException e){
                        GlobalFunctions.scheduleNextSynchronization(mContext, 3600000);
                        ((SyncService)mContext).stopSelf();
                    }
                }
            });
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_HOME)){
            if (login_home_finished < 1){
                login_home_finished++;
                return;
            }

            renewal_page_finished = 0;

            view.loadUrl(GlobalConstants.URL_LIBRARY_RENEWAL);
        }
        else if (url.contains(GlobalConstants.URL_LIBRARY_RENEWAL)){
            if (renewal_page_finished < 1){
                renewal_page_finished++;
                return;
            }

            login_page_finished = 0;
            login_home_finished = 0;

            String script = String.format("%1$s \ngetRenewals();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/renewal_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    try{
                        JSONObject result = new JSONObject(value);
                        if (result.getBoolean("connected"))
                            setReservationBooks(result.getBoolean("hasErrorDiv") ? new JSONArray() :
                                    result.getJSONArray("renewalBooks"));
                        else view.loadUrl(GlobalConstants.URL_LIBRARY_LOGIN);
                    } catch (JSONException e){
                        GlobalFunctions.scheduleNextSynchronization(mContext, 3600000);
                        ((SyncService)mContext).stopSelf();
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        Log.v("Falhou", error.getDescription().toString());

        GlobalFunctions.scheduleNextSynchronization(mContext, 900000);
        ((SyncService)mContext).stopSelf();
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        Log.v("Falhou", failingUrl);

        GlobalFunctions.scheduleNextSynchronization(mContext, 900000);
        ((SyncService)mContext).stopSelf();
    }

    private void setReservationBooks(JSONArray jsResultsArr){
        ArrayList<BookRenewalProperties> availableBooks = new ArrayList<>();

        try {
            availableBooks.clear();
            for (int i = 0; i < jsResultsArr.length(); ++i){
                JSONObject jsBook = jsResultsArr.getJSONObject(i);

                BookRenewalProperties newBook = new BookRenewalProperties();
                newBook.setId(i);
                newBook.setTitle(jsBook.getString("title"));
                newBook.setLibrary(jsBook.getString("library"));
                newBook.setPatrimony(jsBook.getString("patrimony"));
                newBook.setDate(jsBook.getString("date"));
                newBook.setRenewalLink(jsBook.getString("renewal_link"));

                availableBooks.add(newBook);
            }
            bindAlarms(availableBooks);
            GlobalFunctions.scheduleNextSynchronization(mContext, 172800000);
        }catch (JSONException e){
            GlobalFunctions.scheduleNextSynchronization(mContext, 3600000);
            ((SyncService)mContext).stopSelf();
        }
    }

    private void bindAlarms(ArrayList<BookRenewalProperties> availableBooks){
        BookRenewalDAO dao = BookRenewalDatabaseSingletonFactory.getInstance().bookRenewalDAO();
        GlobalFunctions.cancelExistingScheduledAlarms(mContext, dao);

        dao.removeAll();
        for (BookRenewalProperties b: availableBooks) dao.insert(b);

        GlobalFunctions.scheduleRenewalAlarms(mContext, dao);
        GlobalFunctions.scheduleSyncNotification(mContext, 432000000);
    }

    //TODO: Move to GlobalFunctions with aditional parameter
    private void requestUpdateNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, GlobalConstants.CHANNEL_SYNC_ID);
        builder.setSmallIcon(R.drawable.ic_default_book)
                .setContentTitle(mContext.getString(R.string.notification_sync_error_title))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] {750, 750})
                .setColor(ContextCompat.getColor(mContext, android.R.color.holo_red_dark))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(mContext.getString(R.string.notification_sync_error_message))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(mContext.getString(R.string.notification_sync_error_message)))
                .setAutoCancel(true);

        Intent renewalActivity = new Intent(mContext, RenewalActivity.class);
        PendingIntent activity = PendingIntent.getActivity(mContext, GlobalConstants.ACTIVITY_RENEWAL_REQUEST_CODE,
                renewalActivity, 0);
        builder.setContentIntent(activity);

        int notificationID = GlobalConstants.SYNC_REQUEST_INTENT_ID;
        NotificationManager notificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(notificationID, builder.build());
    }
}

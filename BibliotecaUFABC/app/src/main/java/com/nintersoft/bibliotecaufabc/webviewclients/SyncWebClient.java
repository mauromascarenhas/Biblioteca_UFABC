package com.nintersoft.bibliotecaufabc.webviewclients;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.nintersoft.bibliotecaufabc.R;
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
    }

    private void loadSettings(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        GlobalVariables.storeUserFormData = pref.getBoolean(mContext.getString(R.string.key_privacy_store_password), true);

        user_login = pref.getString(mContext.getString(R.string.key_privacy_login_username), "");
        user_password = pref.getString(mContext.getString(R.string.key_privacy_login_password), "");

        if (user_login == null || user_password == null)
            ((SyncService)mContext).finish();
        if (!GlobalVariables.storeUserFormData || user_login.isEmpty()
                || user_password.isEmpty())
            ((SyncService)mContext).finish();
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

        if (url.contains(GlobalConstants.URL_ACCESS_PAGE)){
            if (login_page_finished == 0){
                String script = String.format("%1$s \nperformLogin(\"%2$s\",\"%3$s\");",
                        GlobalFunctions.getScriptFromAssets(mContext, "javascript/login_scraper.js"),
                        user_login, user_password);
                view.evaluateJavascript(script, null);
                login_page_finished++;

                ((SyncService)mContext).killLater();
            }

            String script = String.format("%1$s \ncheckForErrors();",
                    GlobalFunctions.getScriptFromAssets(mContext, "javascript/login_scraper.js"));
            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(final String value) {
                    try {
                        JSONObject result = new JSONObject(value);
                        if (result.getBoolean("hasFormError")) {
                            GlobalFunctions.createSyncNotification(mContext,
                                    R.string.notification_sync_error_title,
                                    R.string.notification_sync_error_message,
                                    GlobalConstants.SYNC_NOTIFICATION_UPDATE_ID);
                            ((SyncService)mContext).finish();
                        }
                    } catch (JSONException e){
                        ((SyncService)mContext).retryAndFinish();
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
                        ((SyncService)mContext).retryAndFinish();
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        ((SyncService)mContext).retryAndFinish();
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        ((SyncService)mContext).retryAndFinish();
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
            //_DEBUG: Remove function call and scope
            GlobalFunctions.writeToFile(jsResultsArr.toString(), "complete");
            ((SyncService)mContext).finish();
        }catch (JSONException e){
            ((SyncService)mContext).retryAndFinish();
        }
    }

    private void bindAlarms(ArrayList<BookRenewalProperties> availableBooks){
        BookRenewalDAO dao = BookRenewalDatabaseSingletonFactory.getInstance().bookRenewalDAO();
        GlobalFunctions.cancelExistingScheduledAlarms(mContext, dao);

        dao.removeAll();
        for (BookRenewalProperties b: availableBooks) dao.insert(b);

        GlobalFunctions.scheduleRenewalAlarms(mContext, dao);
        GlobalFunctions.scheduleSyncNotification(mContext, 432000000);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(mContext.getString(R.string.key_synchronization_schedule), System.currentTimeMillis());
        editor.apply();
    }
}

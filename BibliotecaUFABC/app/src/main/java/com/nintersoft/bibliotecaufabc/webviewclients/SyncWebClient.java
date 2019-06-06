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
import com.nintersoft.bibliotecaufabc.SyncActivity;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.utilities.GlobalVariables;

import org.json.JSONException;
import org.json.JSONObject;

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

        if (user_login == null || user_password == null){
            //TODO: Schedule notification for the next day
            ((SyncActivity)mContext).finish();
        }
        if (!GlobalVariables.storeUserFormData || user_login.isEmpty()
                || user_password.isEmpty()){
            //TODO: Schedule notification for the next day
            ((SyncActivity)mContext).finish();
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    @Override
    public void onPageFinished(final WebView view, String url) {
        super.onPageFinished(view, url);

        //TODO: Perform synchronization
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
                        if (result.getBoolean("hasFormError"))
                            //TODO: Perform a check and alert user
                            ;
                    } catch (JSONException e){
                        //TODO: Schedule for the next hour or do another thing
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
            //TODO: Check if connected an then connect if not
            //TODO: Conditional to set whether the user is able to sync data and then sync it

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
                        if (result.getBoolean("connected")){
                            if (result.getBoolean("hasErrorDiv"))
                                //TODO: There is no book. Does it make any difference?
                                ;
                            else {
                                //TODO: Parse results (See the next line)
                                //((RenewalActivity)mContext).setReservationBooks(result.getJSONArray("renewalBooks"));
                            }
                        }
                        else view.loadUrl(GlobalConstants.URL_LIBRARY_LOGIN);
                    } catch (JSONException e){
                        //TODO: Schedule for the next hour or do another thing
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        //TODO: Schedule notification for the next hour
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);

        //TODO: Schedule notification for the next hour
    }

    private void syncData(){
        //TODO: Perform sync
    }
}

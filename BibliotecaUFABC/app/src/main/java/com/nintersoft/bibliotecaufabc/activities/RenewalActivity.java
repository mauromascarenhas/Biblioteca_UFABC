package com.nintersoft.bibliotecaufabc.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDatabaseSingletonFactory;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalProperties;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.utilities.GlobalVariables;
import com.nintersoft.bibliotecaufabc.viewadapter.RenewalBookAdapter;
import com.nintersoft.bibliotecaufabc.webviewclients.RenewalWebClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RenewalActivity extends AppCompatActivity {

    private WebView dataSource;
    private AlertDialog renewal_load;
    private RecyclerView result_list;
    private LinearLayout layout_error;
    private LinearLayout layout_holder;
    private LinearLayout layout_loading;
    private LinearLayout layout_no_books;

    private BookRenewalDAO dao;
    private RenewalBookAdapter adapter;
    private ArrayList<BookRenewalProperties> availableBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renewal);

        setWebViewSettings();
        bindComponents();
        setupInterface(false);
        setupBookList();
        setListeners();
    }

    private void bindComponents(){
        dao = BookRenewalDatabaseSingletonFactory.getInstance().bookRenewalDAO();

        result_list = findViewById(R.id.list_renewal_results);
        layout_error = findViewById(R.id.renewal_error_layout);
        layout_holder = findViewById(R.id.renewal_holder_layout);
        layout_loading = findViewById(R.id.renewal_loading_layout);
        layout_no_books = findViewById(R.id.no_renewal_books_layout);

        availableBooks = new ArrayList<>();
        adapter = new RenewalBookAdapter(this, availableBooks);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setupInterface(boolean setResults){
        if (setResults){
            FrameLayout.LayoutParams noGravity = (FrameLayout.LayoutParams)layout_holder.getLayoutParams();
            noGravity.gravity = Gravity.NO_GRAVITY;
            layout_holder.setLayoutParams(noGravity);
            result_list.setVisibility(View.VISIBLE);
            layout_loading.setVisibility(View.GONE);
        }
        else{
            FrameLayout.LayoutParams gravity = (FrameLayout.LayoutParams)layout_holder.getLayoutParams();
            gravity.gravity = Gravity.CENTER;
            layout_holder.setLayoutParams(gravity);
            result_list.setVisibility(View.GONE);
            layout_loading.setVisibility(View.VISIBLE);
        }
        layout_error.setVisibility(View.GONE);
        layout_no_books.setVisibility(View.GONE);
    }

    public void setErrorForm(String description){
        if (renewal_load != null)
            if (renewal_load.isShowing()) renewal_load.dismiss();

        result_list.setVisibility(View.GONE);
        layout_loading.setVisibility(View.GONE);
        layout_no_books.setVisibility(View.GONE);
        layout_error.setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.renewal_connection_error_text)).setText(getString(R.string.label_renewal_connection_error, description));
    }

    public void setUserNameNoRenewal(String userName){
        result_list.setVisibility(View.GONE);
        layout_error.setVisibility(View.GONE);
        layout_loading.setVisibility(View.GONE);
        layout_no_books.setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.no_renewal_username)).setText(getString(R.string.label_no_renewal_username, userName));
    }

    private void setupBookList(){
        adapter = new RenewalBookAdapter(this, availableBooks);
        result_list.setLayoutManager(new LinearLayoutManager(this));
        result_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setListeners() {
        findViewById(R.id.button_renewal_reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupInterface(false);
                dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RENEWAL);
            }
        });
    }

    private void setWebViewSettings(){
        dataSource = new WebView(this);
        GlobalFunctions.configureStandardWebView(dataSource);
        dataSource.setWebViewClient(new RenewalWebClient(this));
        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RENEWAL);
    }

    @Override
    protected void onStart() {
        super.onStart();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) notificationManager.cancelAll();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUserDisconnected(){
        Intent login = new Intent(this, LoginActivity.class);
        startActivityForResult(login, GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RENEWAL);
                String username = data.getStringExtra(GlobalConstants.CONNECTED_STATUS_USER_NAME);
                GlobalVariables.bookViewerUserNameSet = username == null ? "???" : username;
                Snackbar.make(layout_holder, getString(R.string.snack_message_connected,
                        username == null ? "???" : username),
                        Snackbar.LENGTH_LONG).show();
            }
            else setErrorForm(getString(R.string.message_renewal_connected_failed));
        }
    }

    public void setReservationBooks(JSONArray jsResultsArr){
        if (jsResultsArr.length() == 0){
            setUserNameNoRenewal("???");
            return;
        }

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
            adapter.notifyDataSetChanged();
            bindAlarms();
            clearNotifications();
        }catch (JSONException e){
            Snackbar.make(layout_holder,
                    R.string.snack_message_parse_fail, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    public void loadRenewalLink(String url){
        dataSource.loadUrl(url);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.message_progress_dialog);
        builder.setCancelable(false);

        renewal_load = builder.create();
        renewal_load.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                TextView message = renewal_load.findViewById(R.id.label_message_loading);
                if (message != null) message.setText(R.string.dialog_warning_message_loading_server_response);
            }
        });
        renewal_load.show();
    }

    public void showRenewalMessage(String message){
        if (renewal_load.isShowing()) renewal_load.dismiss();
        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RENEWAL);
        StringBuilder parsed = new StringBuilder();

        try {
            JSONObject generalA = new JSONObject(message);
            if (generalA.getString("featured") != null)
                parsed.append(generalA.getString("featured"))
                        .append("\n\n");

            JSONArray general = generalA.getJSONArray("details");
            for (int i = 0; i < general.length() - 1; ++i)
                parsed.append(general.getString(i)).append("\n");
            parsed.append(general.getString(general.length() - 1)).append("\n");
        } catch (JSONException e){
            Snackbar.make(layout_holder,
                    R.string.snack_message_parse_fail, Snackbar.LENGTH_LONG)
                    .show();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_server_response_title);
        builder.setMessage(parsed.toString());
        builder.setPositiveButton(R.string.dialog_button_ok, null);
        builder.create().show();
    }

    private void bindAlarms(){
        GlobalFunctions.cancelExistingScheduledAlarms(this, dao);

        dao.removeAll();
        for (BookRenewalProperties b: availableBooks) dao.insert(b);

        GlobalFunctions.scheduleRenewalAlarms(this, dao);
        GlobalFunctions.scheduleSyncNotification(this, 432000000);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(getString(R.string.key_synchronization_schedule), System.currentTimeMillis());
        editor.apply();
    }

    private void clearNotifications(){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) notificationManager.cancelAll();
    }
}
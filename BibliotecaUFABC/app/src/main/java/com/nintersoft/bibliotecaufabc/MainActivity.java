package com.nintersoft.bibliotecaufabc;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nintersoft.bibliotecaufabc.book_search_model.BookSearchDAO;
import com.nintersoft.bibliotecaufabc.book_search_model.BookSearchDatabaseSingletonFactory;
import com.nintersoft.bibliotecaufabc.book_search_model.BookSearchProperties;
import com.nintersoft.bibliotecaufabc.synchronization.SyncService;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.utilities.GlobalVariables;
import com.nintersoft.bibliotecaufabc.viewadapter.SearchBookAdapter;
import com.nintersoft.bibliotecaufabc.webviewclients.MainWebClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    enum PermReqState{
        BEFORE_REQ,
        REQUESTING,
        REQUESTING_LOGIN,
        REQUESTING_SYNC,
        REQUESTED
    }

    private Menu navViewMenu;
    private WebView dataSource;
    private AlertDialog loading_alert;
    private RecyclerView list;
    private MainWebClient dataClient;
    private FloatingActionButton fab;

    private BookSearchDAO dao;
    private SearchBookAdapter adapter;
    private ArrayList<BookSearchProperties> availableBooks;

    private PermReqState permReqState;

    private boolean isFirstRequest;
    private boolean hasRequestedSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindComponents();
        GlobalFunctions.createSyncNotificationChannel(getApplicationContext());
        GlobalFunctions.createRenewalNotificationChannel(getApplicationContext());
        loadPreferences();
        setWebViewSettings();
        setupBookList();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (GlobalVariables.bookViewerUserNameSet != null){
            GlobalVariables.bookViewerUserNameSet = null;
            navViewMenu.findItem(R.id.nav_logout).setTitle(getString(R.string.nav_menu_logout,
                                                                        GlobalVariables.bookViewerUserNameSet));
            setUserConnected(true);

            dataClient.resetCounters();
            dataSource.loadUrl(GlobalConstants.URL_LIBRARY_NEWEST);
        }
    }

    private void bindComponents(){
        dao = BookSearchDatabaseSingletonFactory.getInstance().bookSearchDAO();

        availableBooks = new ArrayList<>();

        fab = findViewById(R.id.fab);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navViewMenu = navigationView.getMenu();
        navViewMenu.findItem(R.id.nav_login).setVisible(false);
        navViewMenu.findItem(R.id.nav_logout).setVisible(false);
        navViewMenu.findItem(R.id.nav_renew).setVisible(false);
        navViewMenu.findItem(R.id.nav_loans).setVisible(false);
        navViewMenu.findItem(R.id.nav_reservation).setVisible(false);

        list = findViewById(R.id.list_items_home);
        permReqState = PermReqState.BEFORE_REQ;
    }

    private void loadPreferences(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        GlobalVariables.showShare = pref.getBoolean(getString(R.string.key_general_share_app_enabled), true);
        GlobalVariables.keepCache = pref.getBoolean(getString(R.string.key_privacy_cache_main_content), true);
        GlobalVariables.ringAlarm = pref.getBoolean(getString(R.string.key_notification_enable_warning), true);
        GlobalVariables.showExtWarning = pref.getBoolean(getString(R.string.key_general_leave_warning), true);
        GlobalVariables.storeUserFormData = pref.getBoolean(getString(R.string.key_privacy_store_password), true);
        GlobalVariables.loginAutomatically = pref.getBoolean(getString(R.string.key_privacy_auto_login), true);

        String syncInterval = pref.getString(getString(R.string.key_notification_sync_interval), "2");
        GlobalVariables.syncInterval = Integer.parseInt(syncInterval == null ? "2" : syncInterval);

        String warningDelay = pref.getString(getString(R.string.key_notification_warning_delay), "0");
        GlobalVariables.ringAlarmOffset = Integer.parseInt(warningDelay == null ? "0" : warningDelay);

        isFirstRequest = true;
        hasRequestedSync = false;

        applyPreferences(pref);
    }

    private void applyPreferences(@Nullable SharedPreferences preferences){
        navViewMenu.findItem(R.id.nav_share).setVisible(GlobalVariables.showShare);

        if (!GlobalVariables.storeUserFormData && preferences != null){
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(getString(R.string.key_privacy_login_username));
            editor.remove(getString(R.string.key_privacy_login_password));
            editor.apply();
        }
    }

    private void setupBookList(){
        adapter = new SearchBookAdapter(this, availableBooks);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setWebViewSettings(){
        dataSource = new WebView(this);
        dataClient = new MainWebClient(this);
        GlobalFunctions.configureStandardWebView(dataSource);

        dataSource.setWebViewClient(dataClient);
        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_NEWEST);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.message_progress_dialog);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Snackbar.make(findViewById(R.id.list_items_home), R.string.snack_message_loading_newest,
                        Snackbar.LENGTH_INDEFINITE).show();
            }
        });
        loading_alert = builder.create();

        loading_alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                requestSyncPermission();
            }
        });
        loading_alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                requestSyncPermission();
            }
        });
        loading_alert.show();
    }

    private void setListeners(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearchDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent acIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(acIntent, GlobalConstants.ACTIVITY_SETTINGS_REQUEST_CODE);
                return true;
            case R.id.action_refresh:
                dataClient.resetCounters();
                dataSource.loadUrl(GlobalConstants.URL_LIBRARY_NEWEST);
                Snackbar.make(findViewById(R.id.list_items_home),
                        R.string.snack_message_refreshing_newest, Snackbar.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull  MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_search: openSearchDialog(); break;
            case R.id.nav_renew: {
                Intent acIntent = new Intent(this, RenewalActivity.class);
                startActivityForResult(acIntent, GlobalConstants.ACTIVITY_RENEWAL_REQUEST_CODE);
                break;
            }
            case R.id.nav_loans: {
                Intent acIntent = new Intent(this, BookLoansActivity.class);
                startActivityForResult(acIntent, GlobalConstants.ACTIVITY_SETTINGS_REQUEST_CODE);
                break;
            }
            case R.id.nav_manage: {
                Intent acIntent = new Intent(this, SettingsActivity.class);
                startActivityForResult(acIntent, GlobalConstants.ACTIVITY_SETTINGS_REQUEST_CODE);
                break;
            }
            case R.id.nav_reservation: {
                Intent acIntent = new Intent(this, ReservationActivity.class);
                startActivity(acIntent);
                break;
            }
            case R.id.nav_login: {
                Intent acIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(acIntent, GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE);
                break;
            }
            case R.id.nav_logout:
                dataSource.loadUrl(GlobalConstants.URL_LIBRARY_LOGOUT);
                navViewMenu.findItem(R.id.nav_login).setVisible(false);
                navViewMenu.findItem(R.id.nav_logout).setVisible(false);
                break;
            case R.id.nav_share: {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_structure));
                if (share.resolveActivity(getPackageManager()) != null)
                    startActivity(Intent.createChooser(share, getString(R.string.intent_share_app)));
                break;
            }
            case R.id.nav_quit: finish(); break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        if (!GlobalVariables.keepCache){
            // Clear Glide cache
            Glide.get(getApplicationContext()).clearDiskCache();

            // Clear WebView cache
            dataSource.clearCache(true);
            dataSource.clearFormData();
            dataSource.clearHistory();
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE){
            if (resultCode == RESULT_OK  && data != null){
                String userName = data.getStringExtra(GlobalConstants.CONNECTED_STATUS_USER_NAME);
                navViewMenu.findItem(R.id.nav_logout).setTitle(getString(R.string.nav_menu_logout, userName == null ? "???" : userName));
                setUserConnected(true);

                dataClient.resetCounters();
                dataSource.loadUrl(GlobalConstants.URL_LIBRARY_NEWEST);
            }
            if (!hasRequestedSync){
                GlobalFunctions.scheduleNextSynchronization(this, GlobalFunctions.nextStandardSync());
                ContextCompat.startForegroundService(this,
                        new Intent(this, SyncService.class).putExtra("service", false));
                hasRequestedSync = true;
            }
        }
        else if (requestCode == GlobalConstants.ACTIVITY_SETTINGS_REQUEST_CODE)
            loadPreferences();
        else if (requestCode == GlobalConstants.SYNC_PERMISSION_REQUEST_ID){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.notification_sync_denied_title)
                            .setMessage(R.string.notification_sync_denied_message)
                            .setPositiveButton(R.string.dialog_button_ok, null)
                            .create().show();
                }

                if (permReqState == PermReqState.REQUESTING_LOGIN){
                    permReqState = PermReqState.REQUESTED;
                    if (GlobalVariables.loginAutomatically){
                        Intent acIntent = new Intent(this, LoginActivity.class);
                        startActivityForResult(acIntent, GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE);
                    }
                    else {
                        GlobalFunctions.scheduleNextSynchronization(this, GlobalFunctions.nextStandardSync());
                        ContextCompat.startForegroundService(this,
                                new Intent(this, SyncService.class).putExtra("service", false));
                        hasRequestedSync = true;
                    }
                    isFirstRequest = false;
                }
                else {
                    GlobalFunctions.scheduleNextSynchronization(this, GlobalFunctions.nextStandardSync());
                    ContextCompat.startForegroundService(this,
                            new Intent(this, SyncService.class).putExtra("service", false));
                    hasRequestedSync = true;
                }
            }
        }
    }

    private void openSearchDialog(){
        final Intent acIntent = new Intent(this, SearchActivity.class);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(getResources().getDimensionPixelSize(R.dimen.dialog_view_margin), 0,
                getResources().getDimensionPixelSize(R.dimen.dialog_view_margin), 0);
        input.setLayoutParams(params);
        layout.addView(input);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.dialog_input_title_search);
        alertBuilder.setView(layout);
        alertBuilder.setPositiveButton(R.string.dialog_button_search, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String terms = input.getText().toString();
                if (terms.isEmpty()) return;

                acIntent.putExtra("terms", input.getText().toString());
                startActivityForResult(acIntent, GlobalConstants.ACTIVITY_SEARCH_REQUEST_CODE);
                dialog.dismiss();
            }
        });
        alertBuilder.create().show();
    }

    public void setSearchResults(String results){
        if (results.isEmpty()) return;

        try {
            dao.removeAll();
            availableBooks.clear();
            JSONArray jsResultsArr = new JSONArray(results);
            for (int i = 0; i < jsResultsArr.length(); ++i){
                JSONObject jsBook = jsResultsArr.getJSONObject(i);
                BookSearchProperties newBook = new BookSearchProperties();
                newBook.setTitle(jsBook.getString("title"));
                newBook.setAuthor(jsBook.getString("author"));
                newBook.setSection(jsBook.getString("section"));
                newBook.setType(jsBook.getString("type"));
                newBook.setCode(jsBook.getString("code"));
                newBook.setId(i);
                availableBooks.add(newBook);
                dao.insert(newBook);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Snackbar.make(findViewById(R.id.list_items_home),
                    R.string.snack_message_parse_fail, Snackbar.LENGTH_LONG)
                    .show();
        } finally {
            navViewMenu.findItem(R.id.nav_loans).setVisible(false);
            if (loading_alert.isShowing()) loading_alert.dismiss();
            else Snackbar.make(findViewById(R.id.list_items_home), R.string.snack_message_loaded_newest,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    public void setUserConnected(boolean connected, String userName){
        navViewMenu.findItem(R.id.nav_logout).setTitle(getString(R.string.nav_menu_logout, userName == null ? "???" : userName));
        setUserConnected(connected);
    }

    synchronized private void setUserConnected(boolean connected){
        navViewMenu.findItem(R.id.nav_login).setVisible(!connected);
        navViewMenu.findItem(R.id.nav_logout).setVisible(connected);
        navViewMenu.findItem(R.id.nav_renew).setVisible(connected);
        navViewMenu.findItem(R.id.nav_reservation).setVisible(connected);

        if (isFirstRequest){
            if (permReqState == PermReqState.REQUESTING || permReqState == PermReqState.BEFORE_REQ){
                permReqState = connected ? PermReqState.REQUESTING_SYNC : PermReqState.REQUESTING_LOGIN;
                return;
            }

            if (!connected && GlobalVariables.loginAutomatically){
                Intent acIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(acIntent, GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE);
            }
            else {
                GlobalFunctions.scheduleNextSynchronization(this, GlobalFunctions.nextStandardSync());
                ContextCompat.startForegroundService(this,
                        new Intent(this, SyncService.class).putExtra("service", false));
                hasRequestedSync = true;
            }
            isFirstRequest = false;
        }
    }

    public void receiveError(String message){
        if (loading_alert.isShowing()) loading_alert.dismiss();

        Snackbar snack_error = Snackbar.make(findViewById(R.id.list_items_home),
                getString(R.string.snack_message_loading_main_error, message), Snackbar.LENGTH_INDEFINITE);
        snack_error.setAction(R.string.snack_button_reload,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataClient.resetCounters();
                        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_NEWEST);
                        Snackbar.make(findViewById(R.id.list_items_home),
                                R.string.snack_message_loading_newest, Snackbar.LENGTH_LONG).show();
                    }
                });
        snack_error.show();

        availableBooks.clear();
        availableBooks.addAll(dao.getAll());
        adapter.notifyDataSetChanged();

        if (GlobalVariables.ringAlarm) navViewMenu.findItem(R.id.nav_loans).setVisible(true);
    }

    synchronized public void requestSyncPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            permReqState = permReqState == PermReqState.BEFORE_REQ ? PermReqState.REQUESTING : PermReqState.REQUESTING_LOGIN;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.notification_sync_rationale_title)
                    .setMessage(R.string.notification_sync_rationale_message)
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            if (intent.resolveActivity(getPackageManager()) != null)
                                startActivityForResult(intent, GlobalConstants.SYNC_PERMISSION_REQUEST_ID);
                        }
                    }).create().show();
        }
        else if (permReqState == PermReqState.REQUESTING_LOGIN) {
            Intent acIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(acIntent, GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE);
            permReqState = PermReqState.REQUESTED;
        }
        else if (permReqState == PermReqState.REQUESTING_SYNC){
            GlobalFunctions.scheduleNextSynchronization(this, GlobalFunctions.nextStandardSync());
            ContextCompat.startForegroundService(this,
                    new Intent(this, SyncService.class).putExtra("service", false));
            hasRequestedSync = true;
        }
        else permReqState = PermReqState.REQUESTED;
    }
}

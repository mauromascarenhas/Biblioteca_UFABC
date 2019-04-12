package com.nintersoft.bibliotecaufabc;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nintersoft.bibliotecaufabc.bookproperties.BookProperties;
import com.nintersoft.bibliotecaufabc.constants.GlobalConstants;
import com.nintersoft.bibliotecaufabc.jsinterface.MainJSInterface;
import com.nintersoft.bibliotecaufabc.viewadapter.SearchBookAdapter;
import com.nintersoft.bibliotecaufabc.webviewclients.MainWebClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

    private Menu navViewMenu;
    private WebView dataSource;
    private AlertDialog loading_alert;
    private RecyclerView list;
    private SearchBookAdapter adapter;
    private FloatingActionButton fab;

    private MainWebClient dataClient;
    private ArrayList<BookProperties> availableBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindComponents();
        loadPreferences();
        setWebViewSettings();
        setupBookList();
        setListeners();
    }

    private void bindComponents(){
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
        navViewMenu.findItem(R.id.nav_reservation).setVisible(false);

        list = findViewById(R.id.list_items_home);
    }

    private void loadPreferences(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        GlobalConstants.showShare = pref.getBoolean(getString(R.string.key_general_share_app_enabled), true);
        GlobalConstants.keepCache = pref.getBoolean(getString(R.string.key_privacy_cache_main_content), true);
        GlobalConstants.showExtWarning = pref.getBoolean(getString(R.string.key_general_leave_warning), true);
        GlobalConstants.storeUserFormData = pref.getBoolean(getString(R.string.key_privacy_store_password), true);

        applyPreferences(pref);
    }

    private void applyPreferences(@Nullable SharedPreferences preferences){
        navViewMenu.findItem(R.id.nav_share).setVisible(GlobalConstants.showShare);

        if (!GlobalConstants.storeUserFormData && preferences != null){
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

    @SuppressLint("AddJavascriptInterface")
    private void setWebViewSettings(){
        dataSource = new WebView(this);
        dataClient = new MainWebClient(this);
        GlobalConstants.configureStandardWebView(dataSource);

        dataSource.setWebViewClient(dataClient);
        dataSource.addJavascriptInterface(new MainJSInterface(this), "js_api");
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
        loading_alert.show();

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
        if (!GlobalConstants.keepCache){
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

        if (requestCode == GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE && resultCode == RESULT_OK
                && data != null){
            String userName = data.getStringExtra("user_name");
            navViewMenu.findItem(R.id.nav_logout).setTitle(getString(R.string.nav_menu_logout, userName == null ? "???" : userName));
            setUserConnected(true);

            dataClient.resetCounters();
            dataSource.loadUrl(GlobalConstants.URL_LIBRARY_NEWEST);
        }
        else if (requestCode == GlobalConstants.ACTIVITY_SETTINGS_REQUEST_CODE)
            loadPreferences();
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
            availableBooks.clear();
            JSONArray jsResultsArr = new JSONArray(results);
            for (int i = 0; i < jsResultsArr.length(); ++i){
                JSONObject jsBook = jsResultsArr.getJSONObject(i);
                BookProperties newBook = new BookProperties();
                newBook.setTitle(jsBook.getString("title"));
                newBook.setAuthor(jsBook.getString("author"));
                newBook.setSection(jsBook.getString("section"));
                newBook.setType(jsBook.getString("type"));
                newBook.setCode(jsBook.getString("code"));
                availableBooks.add(newBook);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (loading_alert.isShowing()) loading_alert.dismiss();
            else Snackbar.make(findViewById(R.id.list_items_home), R.string.snack_message_loaded_newest,
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    public void setUserConnected(boolean connected, String userName){
        navViewMenu.findItem(R.id.nav_logout).setTitle(getString(R.string.nav_menu_logout, userName == null ? "???" : userName));
        setUserConnected(connected);
    }

    private void setUserConnected(boolean connected){
        navViewMenu.findItem(R.id.nav_login).setVisible(!connected);
        navViewMenu.findItem(R.id.nav_logout).setVisible(connected);
        navViewMenu.findItem(R.id.nav_renew).setVisible(connected);
        navViewMenu.findItem(R.id.nav_reservation).setVisible(connected);
        GlobalConstants.isUserConnected = connected;
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
    }
}
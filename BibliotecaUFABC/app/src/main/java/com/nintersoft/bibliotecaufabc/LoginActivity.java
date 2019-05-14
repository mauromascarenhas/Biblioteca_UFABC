package com.nintersoft.bibliotecaufabc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.jsinterface.LoginJSInterface;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.webviewclients.LoginWebClient;

public class LoginActivity extends AppCompatActivity {

    private WebView dataSource;
    private EditText edt_login;
    private EditText edt_password;
    private ScrollView loginForm;
    private LinearLayout errorLayout;
    private LinearLayout progressForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindComponents();
        setupInterface(false);
        setWebViewSettings();
        setListeners();
    }

    private void bindComponents(){
        loginForm = findViewById(R.id.form_login);
        errorLayout = findViewById(R.id.error_layout);
        progressForm = findViewById(R.id.progress_layout);

        edt_login = findViewById(R.id.edit_login);
        edt_password = findViewById(R.id.edit_password);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setupInterface(boolean setLoginForm){
        if (setLoginForm){
            loginForm.setVisibility(View.VISIBLE);
            progressForm.setVisibility(View.GONE);
        }
        else {
            loginForm.setVisibility(View.GONE);
            progressForm.setVisibility(View.VISIBLE);
        }
        errorLayout.setVisibility(View.GONE);
    }

    public void setErrorForm(String description){
        loginForm.setVisibility(View.GONE);
        progressForm.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.login_connection_error_text)).setText(getString(R.string.label_login_connection_error, description));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("AddJavascriptInterface")
    private void setWebViewSettings(){
        dataSource = new WebView(this);
        GlobalFunctions.configureStandardWebView(dataSource);
        dataSource.setWebViewClient(new LoginWebClient(this));
        dataSource.addJavascriptInterface(new LoginJSInterface(this), "js_api");
        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_LOGIN);
    }

    protected void setListeners(){
        final Button btLogin = findViewById(R.id.bt_login);

        findViewById(R.id.bt_retrieve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recIntent = new Intent(Intent.ACTION_VIEW);
                recIntent.setData(Uri.parse("https://acesso.ufabc.edu.br/passwordRecovery/index"));
                startActivity(recIntent);
            }
        });
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        edt_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    performLogin();
                    return true;
                }
                return false;
            }
        });

        findViewById(R.id.button_try_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupInterface(false);
                dataSource.loadUrl(GlobalConstants.URL_LIBRARY_LOGIN);
            }
        });
    }

    public void setSavedUserLogin(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String login = pref.getString(getString(R.string.key_privacy_login_username), "");
        String pass = pref.getString(getString(R.string.key_privacy_login_password), "");

        if ((login == null || pass == null)){
            setupInterface(true);
            return;
        }
        else if (login.isEmpty() || pass.isEmpty()){
            setupInterface(true);
            return;
        }

        edt_login.setText(login);
        edt_password.setText(pass);

        performLogin();
    }

    protected void performLogin(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        String login = edt_login.getText().toString(),
               pass = edt_password.getText().toString();

        boolean proceed = !(login.isEmpty() || pass.isEmpty());
        if (pass.isEmpty()){
            edt_password.setError(getString(R.string.edt_error_form_field_empty));
            edt_password.requestFocus();
        }
        if (login.isEmpty()){
            edt_login.setError(getString(R.string.edt_error_form_field_empty));
            edt_login.requestFocus();
        }
        if (!proceed) return;

        String script = String.format("javascript: %1$s performLogin(\"%2$s\",\"%3$s\");",
                GlobalFunctions.getScriptFromAssets(this, "javascript/login_scraper.js"),
                login, pass);
        GlobalFunctions.executeScript(dataSource, script);
        setupInterface(false);

        if (GlobalConstants.storeUserFormData){
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString(getString(R.string.key_privacy_login_username), login);
            editor.putString(getString(R.string.key_privacy_login_password), pass);
            editor.apply();
        }
    }

    public void hasLoggedIn(String userName){
        Intent data = new Intent();
        data.putExtra(GlobalConstants.CONNECTED_STATUS_USER_NAME, userName);
        setResult(RESULT_OK, data);
        finish();
    }

    public void showLoginError(String message){
        setupInterface(true);
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage(message);
        ab.setPositiveButton(R.string.dialog_button_ok, null);
        ab.create().show();
    }
}

package com.nintersoft.bibliotecaufabc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.jsinterface.DetailsJSInterface;
import com.nintersoft.bibliotecaufabc.jsinterface.ReserveJSInterface;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.webviewclients.DetailsWebClient;
import com.nintersoft.bibliotecaufabc.webviewclients.ReserveWebClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BookViewerActivity extends AppCompatActivity {

    private boolean reservationRequest;

    private String bookURL;
    private String tempObject;

    private Button res_button;
    private WebView dataSource;
    private MenuItem share_action;
    private AlertDialog loading_alert;
    private LinearLayout layout_data;
    private LinearLayout layout_error;
    private LinearLayout layout_holder;
    private LinearLayout layout_loading;

    private DetailsWebClient detailsWebClient;
    private ReserveWebClient reserveWebClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_viewer);
        initializeBookData();

        bindComponents();
        setWebViewSettings();
        setupInterface(false);
        setListeners();
    }

    private void bindComponents(){
        layout_data = findViewById(R.id.book_viewer_layout);
        layout_error = findViewById(R.id.book_viewer_loading_error);
        layout_holder = findViewById(R.id.book_viewer_holder_layout);
        layout_loading = findViewById(R.id.book_viewer_loading_layout);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeBookData(){
        Intent data = getIntent();

        boolean hasData = true;
        String bookData = data.getStringExtra("code");
        if (bookData == null) {
            if (data.getData() != null){
                bookData = data.getData().getQueryParameter("codigo");
                hasData = bookData != null;
            }
            else hasData = false;
        }

        if (hasData) bookURL = GlobalConstants.URL_LIBRARY_DETAILS + "?codigo=" + bookData
                + GlobalConstants.MANDATORY_APPEND_URL_LIBRARY_DETAILS;
        else bookURL = "";

        reservationRequest = false;
    }

    @SuppressLint("AddJavascriptInterface")
    private void setWebViewSettings(){
        dataSource = new WebView(this);
        reserveWebClient = new ReserveWebClient(this);
        detailsWebClient = new DetailsWebClient(this);

        GlobalFunctions.configureStandardWebView(dataSource);
        dataSource.setWebViewClient(detailsWebClient);
        dataSource.addJavascriptInterface(new DetailsJSInterface(this), "js_api");
        dataSource.addJavascriptInterface(new ReserveJSInterface(this), "js_api_r");
        if (!bookURL.isEmpty()) dataSource.loadUrl(bookURL);
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_error_title));
            builder.setMessage(getString(R.string.label_book_details_incorrect_data));
            builder.setNeutralButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BookViewerActivity.this.finish();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }
    }

    public void setupInterface(boolean setResults){
        if (setResults){
            FrameLayout.LayoutParams noGravity = (FrameLayout.LayoutParams)layout_holder.getLayoutParams();
            noGravity.gravity = Gravity.NO_GRAVITY;
            layout_holder.setLayoutParams(noGravity);
            layout_data.setVisibility(View.VISIBLE);
            layout_loading.setVisibility(View.GONE);
        }
        else{
            FrameLayout.LayoutParams gravity = (FrameLayout.LayoutParams)layout_holder.getLayoutParams();
            gravity.gravity = Gravity.CENTER;
            layout_holder.setLayoutParams(gravity);
            layout_data.setVisibility(View.GONE);
            layout_loading.setVisibility(View.VISIBLE);
        }
        layout_error.setVisibility(View.GONE);
    }

    public void setErrorForm(String description){
        layout_data.setVisibility(View.GONE);
        layout_loading.setVisibility(View.GONE);
        layout_error.setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.book_viewer_error_text)).setText(getString(R.string.label_book_details_connection_error, description));
    }

    private void setListeners(){
        findViewById(R.id.book_viewer_button_reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupInterface(false);
                dataSource.reload();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_details, menu);

        share_action = menu.findItem(R.id.action_share_book);
        share_action.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                finish();
                return true;
            case R.id.action_share_book:
                String bookShare = getString(R.string.share_book_structure,
                        ((TextView)findViewById(R.id.label_book_title)).getText().toString(),
                        ((TextView)findViewById(R.id.label_book_author)).getText().toString(),
                        bookURL);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, bookShare);
                if (share.resolveActivity(getPackageManager()) != null)
                    startActivity(Intent.createChooser(share, getString(R.string.intent_share_book)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                reservationRequest = true;
                detailsWebClient.resetCounters();

                dataSource.loadUrl(bookURL);
                String username = data.getStringExtra("user_name");
                Snackbar.make(layout_holder, getString(R.string.snack_message_connected,
                        username == null ? "???" : username),
                        Snackbar.LENGTH_LONG).show();
            }
            else setErrorForm(getString(R.string.message_renewal_connected_failed));
        }
    }

    public void setBookData(String jsObject, boolean isUserConnected){
        if (reservationRequest){
            requestReservation();
            return;
        }

        try {
            final JSONObject book_properties = new JSONObject(jsObject);
            if (!book_properties.getBoolean("exists"))
                return;

            Glide.with(getApplicationContext())
                    .load(GlobalConstants.URL_LIBRARY_BOOK_COVER +
                            GlobalConstants.MANDATORY_APPEND_URL_LIBRARY_BOOK_COVER + book_properties.getString("code"))
                    .placeholder(R.drawable.ic_default_book)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).into((ImageView) findViewById(R.id.image_details_book_cover));

            ((TextView)findViewById(R.id.label_book_title)).setText(book_properties.getString("title"));
            ((TextView)findViewById(R.id.label_book_author)).setText(book_properties.getString("author"));

            int dMargin = (int)(getResources().getDimension(R.dimen.form_items_default_margin));
            LinearLayout.LayoutParams defParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            defParams.setMargins(0, dMargin, 0, 0);

            int hMargin = (int)(getResources().getDimension(R.dimen.form_header_default_margin));
            LinearLayout.LayoutParams hParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            hParams.setMargins(0, hMargin, 0, hMargin);
            hParams.gravity = Gravity.CENTER_HORIZONTAL;

            float headerSize = getResources().getDimension(R.dimen.label_form_title_def_size);
            float descriptionSize = getResources().getDimension(R.dimen.label_form_item_def_size);

            JSONArray properties = book_properties.getJSONArray("properties");
            for (int i = 0; i < properties.length(); ++i){
                JSONObject cProperty = properties.getJSONObject(i);

                TextView header = new TextView(this);
                TextView description = new TextView(this);

                header.setText(cProperty.getString("title"));
                description.setText(cProperty.getString("description"));

                header.setTypeface(header.getTypeface(), Typeface.BOLD);
                header.setTextSize(TypedValue.COMPLEX_UNIT_PX, headerSize);
                header.setLayoutParams(defParams);

                description.setTextSize(TypedValue.COMPLEX_UNIT_PX, descriptionSize);

                layout_data.addView(header);
                layout_data.addView(description);
            }

            JSONArray mediaContent = book_properties.getJSONArray("media");
            if (mediaContent.length() > 0){
                for (int i = 0; i < mediaContent.length(); ++i){
                    JSONObject cMedia = mediaContent.getJSONObject(i);

                    TextView header = new TextView(this);
                    header.setText(cMedia.getString("type"));
                    header.setGravity(Gravity.CENTER_HORIZONTAL);
                    header.setTypeface(header.getTypeface(), Typeface.BOLD);
                    header.setTextSize(TypedValue.COMPLEX_UNIT_PX, headerSize);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        header.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    header.setLayoutParams(hParams);
                    layout_data.addView(header);

                    JSONArray mediaContentValues = cMedia.getJSONArray("values");
                    for (int j = 0; j < mediaContentValues.length(); ++j){
                        JSONObject cValue = mediaContentValues.getJSONObject(i);

                        TextView description = new TextView(this);
                        description.setText(String.format("%1$s %2$s", "\u23E9 ", cValue.getString("text").toUpperCase()));
                        description.setTextSize(TypedValue.COMPLEX_UNIT_PX, descriptionSize);
                        description.setTextColor(getResources().getColor(R.color.colorLink));

                        final String link = cValue.getString("link");
                        if (!link.isEmpty()) {
                            description.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (GlobalConstants.showExtWarning) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(BookViewerActivity.this);
                                        builder.setTitle(R.string.dialog_warning_title);
                                        builder.setMessage(getString(R.string.dialog_warning_message_external_link, link));
                                        builder.setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent browser = new Intent(Intent.ACTION_VIEW);
                                                browser.setData(Uri.parse(link));
                                                if (browser.resolveActivity(getPackageManager()) != null)
                                                    startActivity(browser);
                                            }
                                        });
                                        builder.setNegativeButton(R.string.dialog_button_cancel, null);
                                        builder.create().show();
                                    }
                                    else {
                                        Intent browser = new Intent(Intent.ACTION_VIEW);
                                        browser.setData(Uri.parse(link));
                                        if (browser.resolveActivity(getPackageManager()) != null)
                                            startActivity(browser);
                                    }
                                }
                            });
                        }

                        layout_data.addView(description);
                    }
                }
            }

            JSONArray copies = book_properties.getJSONArray("copies");
            if (copies.length() > 0){
                TextView header = new TextView(this);
                header.setText(R.string.label_book_details_available_header);
                header.setGravity(Gravity.CENTER_HORIZONTAL);
                header.setTypeface(header.getTypeface(), Typeface.BOLD);
                header.setTextSize(TypedValue.COMPLEX_UNIT_PX, headerSize);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    header.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                header.setLayoutParams(hParams);
                layout_data.addView(header);

                for (int i = 0; i < copies.length(); ++i){
                    JSONObject cValue = copies.getJSONObject(i);

                    TextView description = new TextView(this);
                    description.setText(getString(R.string.label_book_details_available_details,
                            cValue.getString("copies").replace("de", getString(R.string.label_book_details_available_counting_sep)),
                            cValue.getString("library")));
                    description.setTextSize(TypedValue.COMPLEX_UNIT_PX, descriptionSize);
                    description.setTextColor(getResources().getColor(R.color.colorLibrary));

                    layout_data.addView(description);
                }
            }

            if (book_properties.getBoolean("reservable")){
                res_button = new Button(this);
                res_button.setText(R.string.button_book_details_reserve);
                res_button.setLayoutParams(hParams);

                if (isUserConnected){
                    res_button.setOnClickListener(new View.OnClickListener() {
                        @SuppressLint("AddJavascriptInterface")
                        @Override
                        public void onClick(View v) {
                            requestReservation();
                        }
                    });
                }
                else {
                    res_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(BookViewerActivity.this);
                            builder.setTitle(R.string.dialog_warning_title);
                            builder.setMessage(R.string.dialog_warning_message_user_disconnected);
                            builder.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent login = new Intent(BookViewerActivity.this, LoginActivity.class);
                                    startActivityForResult(login, GlobalConstants.ACTIVITY_LOGIN_REQUEST_CODE);
                                    setupInterface(false);
                                }
                            });
                            builder.setNegativeButton(R.string.dialog_button_no, null);
                            builder.create().show();
                        }
                    });
                }
                layout_data.addView(res_button);
            }
        } catch (JSONException e) {
            Snackbar.make(layout_holder,
                    R.string.snack_message_parse_fail, Snackbar.LENGTH_LONG)
                    .show();
        } finally {
            share_action.setVisible(true);
        }
    }

    public void requestReservation(){
        reserveWebClient.resetCounters();
        dataSource.setWebViewClient(reserveWebClient);
        GlobalFunctions.executeScript(dataSource, "javascript: reserveBook();");

        AlertDialog.Builder builder = new AlertDialog.Builder(BookViewerActivity.this);
        builder.setView(R.layout.message_progress_dialog);
        builder.setCancelable(false);

        loading_alert = builder.create();
        loading_alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                TextView message = loading_alert.findViewById(R.id.label_message_loading);
                if (message != null) message.setText(R.string.dialog_warning_message_loading_reservation);
            }
        });
        loading_alert.show();

        res_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reservationRequest = true;
                setupInterface(false);

                detailsWebClient.resetCounters();
                dataSource.setWebViewClient(detailsWebClient);
                dataSource.loadUrl(bookURL);
            }
        });
    }

    public void setReservationError(String error){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_error_title);
        builder.setMessage(getString(R.string.dialog_reservation_error_message, error));
        builder.setPositiveButton(R.string.dialog_button_ok, null);
        builder.create().show();
    }

    public void setReservationAvailability(final String optionsObject){
        if(loading_alert.isShowing()) loading_alert.dismiss();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_reservation_options_title);
        builder.setView(R.layout.dialog_reservation_options_layout);
        builder.setPositiveButton(R.string.dialog_button_submit, null);
        builder.setNegativeButton(R.string.dialog_button_cancel, null);
        builder.setCancelable(false);
        final AlertDialog optionsDialog = builder.create();
        optionsDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                try {
                    JSONObject options = new JSONObject(optionsObject);

                    for (int i = 0; i < 5; ++i){
                        Spinner cSpinner;
                        TextView cTextView;
                        JSONArray cOptions;

                        switch (i){
                            case 0:
                                cOptions = options.getJSONArray("library");
                                cTextView = optionsDialog.findViewById(R.id.dialog_reservation_label_library);
                                cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_library);
                                break;
                            case 1:
                                cOptions = options.getJSONArray("volume");
                                cTextView = optionsDialog.findViewById(R.id.dialog_reservation_label_volume);
                                cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_volume);
                                break;
                            case 2:
                                cOptions = options.getJSONArray("year");
                                cTextView = optionsDialog.findViewById(R.id.dialog_reservation_label_year);
                                cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_year);
                                break;
                            case 3:
                                cOptions = options.getJSONArray("edition");
                                cTextView = optionsDialog.findViewById(R.id.dialog_reservation_label_edition);
                                cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_edition);
                                break;
                            default:
                                cOptions = options.getJSONArray("support");
                                cTextView = optionsDialog.findViewById(R.id.dialog_reservation_label_support);
                                cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_support);
                                break;
                        }

                        if (cOptions.length() == 0){
                            if (cTextView != null) cTextView.setVisibility(View.GONE);
                            if (cSpinner != null) cSpinner.setVisibility(View.GONE);
                        }
                        else {
                            ArrayList<String> sOptions = new ArrayList<>();
                            for (int k = 0; k < cOptions.length(); ++k)
                                sOptions.add(cOptions.getString(k));

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(BookViewerActivity.this,
                                    android.R.layout.simple_spinner_item, sOptions){
                                @Override
                                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                    View v = super.getDropDownView(position, convertView, parent);
                                    TextView label = v.findViewById(android.R.id.text1);
                                    label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.label_form_item_def_size));
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                                        label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    label.getLayoutParams();
                                    label.setGravity(Gravity.CENTER);
                                    v.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.spinner_item_size));
                                    return v;
                                }
                            };
                            if (cSpinner != null) cSpinner.setAdapter(adapter);
                        }
                    }
                } catch (JSONException e) {
                    Snackbar.make(layout_holder,
                            R.string.snack_message_parse_fail, Snackbar.LENGTH_LONG)
                            .show();
                }

                Button button = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            boolean hasErrors = false;
                            JSONObject options = new JSONObject();

                            for (int i = 0; i < 5; ++i){
                                Spinner cSpinner;
                                String indexName;
                                switch (i){
                                    case 0:
                                        cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_library);
                                        indexName = "library";
                                        break;
                                    case 1:
                                        cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_volume);
                                        indexName = "volume";
                                        break;
                                    case 2:
                                        cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_year);
                                        indexName = "year";
                                        break;
                                    case 3:
                                        cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_edition);
                                        indexName = "edition";
                                        break;
                                    default:
                                        cSpinner = optionsDialog.findViewById(R.id.dialog_reservation_spinner_support);
                                        indexName = "support";
                                        break;
                                }

                                if (cSpinner != null) {
                                    int cIndex = cSpinner.getSelectedItemPosition();
                                    if (cIndex < 1 && cSpinner.getVisibility() != View.GONE) hasErrors = true;
                                    else options.put(indexName, cIndex);
                                }
                                else options.put(indexName, -1);
                            }

                            if (hasErrors){
                                TextView errorMessage = optionsDialog.findViewById(R.id.dialog_reservation_label_error);
                                if (errorMessage != null) errorMessage.setVisibility(View.VISIBLE);
                                return;
                            }

                            tempObject = options.toString();
                            String script = String.format("javascript: %1$s\nsubmitReservationForm();",
                                    GlobalFunctions.getScriptFromAssets(BookViewerActivity.this,
                                            "javascript/submit_reserve_scraper.js"));
                            GlobalFunctions.executeScript(dataSource, script);
                            optionsDialog.dismiss();

                            loading_alert.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    TextView message = loading_alert.findViewById(R.id.label_message_loading);
                                    if (message != null) message.setText(R.string.dialog_warning_message_loading_server_response);
                                }
                            });
                            loading_alert.show();

                        } catch (JSONException e) {
                            Snackbar.make(layout_holder,
                                    R.string.snack_message_parse_fail, Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
            }
        });
        optionsDialog.show();
    }

    public void setReservationResults(String serverMessage){
        if (loading_alert.isShowing()) loading_alert.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_server_response_title);
        builder.setMessage(serverMessage);
        builder.setPositiveButton(R.string.dialog_button_ok, null);
        builder.create().show();
    }

    public String getTemporaryObject(){
        return tempObject;
    }

}

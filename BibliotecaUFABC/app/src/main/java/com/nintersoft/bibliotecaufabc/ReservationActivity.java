package com.nintersoft.bibliotecaufabc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nintersoft.bibliotecaufabc.bookproperties.BookReservationProperties;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.jsinterface.ReservationJSInterface;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.viewadapter.ReservationBookAdapter;
import com.nintersoft.bibliotecaufabc.webviewclients.ReservationWebClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReservationActivity extends AppCompatActivity {

    private WebView dataSource;
    private AlertDialog cancel_load;
    private LinearLayout layout_error;
    private LinearLayout layout_holder;
    private LinearLayout layout_loading;
    private LinearLayout layout_no_books;
    private RecyclerView reservation_list;

    private ReservationBookAdapter adapter;
    private ArrayList<BookReservationProperties> availableBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        setWebViewSettings();
        bindComponents();
        setupInterface(false);
        setupBookList();
        setListeners();
    }

    private void bindComponents(){
        layout_error = findViewById(R.id.reservation_error_layout);
        layout_holder = findViewById(R.id.reservation_holder_layout);
        layout_loading = findViewById(R.id.reservation_loading_layout);
        layout_no_books = findViewById(R.id.no_reservation_books_layout);
        reservation_list = findViewById(R.id.list_reservation_results);

        availableBooks = new ArrayList<>();
        adapter = new ReservationBookAdapter(this, availableBooks);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setupInterface(boolean setResults){
        if (setResults){
            FrameLayout.LayoutParams noGravity = (FrameLayout.LayoutParams)layout_holder.getLayoutParams();
            noGravity.gravity = Gravity.NO_GRAVITY;
            layout_holder.setLayoutParams(noGravity);
            reservation_list.setVisibility(View.VISIBLE);
            layout_loading.setVisibility(View.GONE);
        }
        else{
            FrameLayout.LayoutParams gravity = (FrameLayout.LayoutParams)layout_holder.getLayoutParams();
            gravity.gravity = Gravity.CENTER;
            layout_holder.setLayoutParams(gravity);
            reservation_list.setVisibility(View.GONE);
            layout_loading.setVisibility(View.VISIBLE);
        }
        layout_error.setVisibility(View.GONE);
        layout_no_books.setVisibility(View.GONE);
    }

    public void setErrorForm(String description){
        if (cancel_load != null)
            if (cancel_load.isShowing()) cancel_load.dismiss();

        layout_loading.setVisibility(View.GONE);
        layout_no_books.setVisibility(View.GONE);
        reservation_list.setVisibility(View.GONE);
        layout_error.setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.reservation_connection_error_text)).setText(getString(R.string.label_reservation_connection_error, description));
    }

    public void setUserNameNoReservation(String userName){
        layout_error.setVisibility(View.GONE);
        layout_loading.setVisibility(View.GONE);
        reservation_list.setVisibility(View.GONE);
        layout_no_books.setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.no_reservation_username)).setText(getString(R.string.label_no_reservation_username, userName));
    }

    private void setupBookList(){
        adapter = new ReservationBookAdapter(this, availableBooks);
        reservation_list.setLayoutManager(new LinearLayoutManager(this));
        reservation_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setListeners() {
        findViewById(R.id.button_reservation_reload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupInterface(false);
                dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RESERVATION);
            }
        });
    }

    @SuppressLint("AddJavascriptInterface")
    private void setWebViewSettings(){
        dataSource = new WebView(this);
        GlobalFunctions.configureStandardWebView(dataSource);
        dataSource.setWebViewClient(new ReservationWebClient(this));
        dataSource.addJavascriptInterface(new ReservationJSInterface(this), "js_api");
        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_RESERVATION);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setReservationBooks(String books){
        try {
            JSONArray jsResultsArr = new JSONArray(books);

            if (jsResultsArr.length() == 0){
                setUserNameNoReservation("???");
                return;
            }

            availableBooks.clear();
            for (int i = 0; i < jsResultsArr.length(); ++i){
                JSONObject jsBook = jsResultsArr.getJSONObject(i);

                BookReservationProperties newBook = new BookReservationProperties();
                newBook.setTitle(jsBook.getString("title"));
                newBook.setQueue(jsBook.getString("queue"));
                newBook.setLibrary(jsBook.getString("library"));
                newBook.setMaterial(jsBook.getString("material"));
                newBook.setSituation(jsBook.getString("situation"));
                newBook.setCancelLink(jsBook.getString("cancel_link"));

                availableBooks.add(newBook);
            }
            adapter.notifyDataSetChanged();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void loadCancellationLink(String url){
        dataSource.loadUrl(url);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.message_progress_dialog);
        builder.setCancelable(false);

        cancel_load = builder.create();
        cancel_load.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                TextView message = cancel_load.findViewById(R.id.label_message_loading);
                if (message != null) message.setText(R.string.dialog_warning_message_loading_server_response);
            }
        });
        cancel_load.show();
    }

    public void showCancellationMessage(String message){
        if (cancel_load.isShowing()) cancel_load.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_server_response_title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.dialog_button_ok, null);
        builder.create().show();
    }
}

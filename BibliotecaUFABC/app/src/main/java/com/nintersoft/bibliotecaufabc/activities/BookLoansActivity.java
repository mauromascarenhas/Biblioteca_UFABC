package com.nintersoft.bibliotecaufabc.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nintersoft.bibliotecaufabc.R;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDAO;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalDatabaseSingletonFactory;
import com.nintersoft.bibliotecaufabc.book_renewal_model.BookRenewalProperties;
import com.nintersoft.bibliotecaufabc.viewadapter.BookLoansAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BookLoansActivity extends AppCompatActivity {

    private RecyclerView result_list;
    private LinearLayout layout_holder;
    private LinearLayout layout_no_books;

    private BookRenewalDAO dao;
    private BookLoansAdapter adapter;
    private ArrayList<BookRenewalProperties> availableBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renewal);

        bindComponents();
        setupInterface(getReservationBooks());
        setupBookList();
    }

    private void bindComponents(){
        dao = BookRenewalDatabaseSingletonFactory.getInstance().bookRenewalDAO();

        result_list = findViewById(R.id.list_renewal_results);
        layout_holder = findViewById(R.id.renewal_holder_layout);
        layout_no_books = findViewById(R.id.no_renewal_books_layout);

        availableBooks = new ArrayList<>();
        adapter = new BookLoansAdapter(availableBooks);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupInterface(boolean setResults){
        if (setResults){
            FrameLayout.LayoutParams noGravity = (FrameLayout.LayoutParams)layout_holder.getLayoutParams();
            noGravity.gravity = Gravity.NO_GRAVITY;
            layout_holder.setLayoutParams(noGravity);
            layout_no_books.setVisibility(View.GONE);
            result_list.setVisibility(View.VISIBLE);
        }
        else{
            FrameLayout.LayoutParams gravity = (FrameLayout.LayoutParams)layout_holder.getLayoutParams();
            gravity.gravity = Gravity.CENTER;
            layout_holder.setLayoutParams(gravity);
            result_list.setVisibility(View.GONE);

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            setUserNameNoRenewal(pref.getString(getString(R.string.key_privacy_login_username), "???"));
        }
        findViewById(R.id.renewal_loading_layout).setVisibility(View.GONE);
        findViewById(R.id.renewal_error_layout).setVisibility(View.GONE);
    }

    private void setUserNameNoRenewal(String userName){
        result_list.setVisibility(View.GONE);
        layout_no_books.setVisibility(View.VISIBLE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.getDefault());
        ((TextView)findViewById(R.id.no_renewal_username)).setText(getString(R.string.label_no_bookloan_username, userName,
                df.format(new Date(preferences.getLong(this.getString(R.string.key_synchronization_schedule), 0)))));
    }

    private void setupBookList(){
        adapter = new BookLoansAdapter(availableBooks);
        result_list.setLayoutManager(new LinearLayoutManager(this));
        result_list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean getReservationBooks(){
        availableBooks = new ArrayList<>(dao.getAll());
        adapter.notifyDataSetChanged();
        return !availableBooks.isEmpty();
    }
}

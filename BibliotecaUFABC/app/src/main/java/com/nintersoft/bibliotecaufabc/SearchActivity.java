package com.nintersoft.bibliotecaufabc;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nintersoft.bibliotecaufabc.book_search_model.BookSearchProperties;
import com.nintersoft.bibliotecaufabc.utilities.GlobalConstants;
import com.nintersoft.bibliotecaufabc.jsinterface.SearchJSInterface;
import com.nintersoft.bibliotecaufabc.utilities.GlobalFunctions;
import com.nintersoft.bibliotecaufabc.viewadapter.SearchBookAdapter;
import com.nintersoft.bibliotecaufabc.webviewclients.SearchWebClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    int field;

    private String searchData;
    private WebView dataSource;
    private RecyclerView result_list;
    private LinearLayout layout_error;
    private LinearLayout layout_holder;
    private LinearLayout layout_loading;
    private SearchBookAdapter adapter;
    private FloatingActionButton fab;
    private FloatingActionButton fab_more;
    private ArrayList<Boolean> searchFilter;
    private ArrayList<Boolean> searchLibrary;
    private ArrayList<BookSearchProperties> availableBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        initializeSearchData();

        setWebViewSettings();
        bindComponents();
        setupInterface(false);
        setDefaultData();
        setListeners();
    }

    private void bindComponents(){
        result_list = findViewById(R.id.list_search_results);
        layout_error = findViewById(R.id.search_error_layout);
        layout_loading = findViewById(R.id.loading_layout);
        layout_holder = findViewById(R.id.holder_layout);

        if (searchFilter == null)
            searchFilter = new ArrayList<>();
        if (searchLibrary == null)
            searchLibrary = new ArrayList<>();
        availableBooks = new ArrayList<>();

        fab = findViewById(R.id.fab);
        fab_more = findViewById(R.id.fab_load_more);

        adapter = new SearchBookAdapter(this, availableBooks);
        result_list.setLayoutManager(new LinearLayoutManager(this));
        result_list.setAdapter(adapter);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setDefaultData(){
        if (searchFilter.isEmpty()) {
            searchFilter.add(true);
            for (int i = 1; i < getResources().getStringArray(R.array.search_options_types).length; ++i)
                searchFilter.add(false);
        }

        if (searchLibrary.isEmpty()) {
            searchLibrary.add(true);
            for (int i = 1; i < getResources().getStringArray(R.array.search_options_library_campus).length; ++i)
                searchLibrary.add(false);
        }
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
    }

    public void setErrorForm(String description){
        result_list.setVisibility(View.GONE);
        layout_loading.setVisibility(View.GONE);
        layout_error.setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.search_connection_error_text)).setText(getString(R.string.label_search_connection_error, description));
    }

    private void setListeners() {
        findViewById(R.id.app_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNewSearch();
            }
        });
        findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNewSearch();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent filters = new Intent(SearchActivity.this, SearchFiltersActivity.class);
                filters.putExtra("field", field);
                filters.putExtra("filters", searchFilter);
                filters.putExtra("libraries", searchLibrary);
                startActivityForResult(filters, GlobalConstants.ACTIVITY_SEARCH_FILTER_REQUEST_CODE);
            }
        });
        fab_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalFunctions.executeScript(dataSource, "javascript: loadMoreBooks();");
                v.setVisibility(View.GONE);

                Snackbar.make(findViewById(R.id.list_search_results),
                        R.string.snack_message_loading_more_items,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initializeSearchData(){
        Intent data = getIntent();

        field = data.getIntExtra("field", 0);
        searchFilter = (ArrayList<Boolean>) data.getSerializableExtra("filters");
        searchLibrary = (ArrayList<Boolean>) data.getSerializableExtra("libraries");

        searchData = data.getStringExtra("terms");

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(searchData);
    }

    public String getSearchData(){
        return searchData;
    }

    @SuppressLint("AddJavascriptInterface")
    private void setWebViewSettings(){
        dataSource = new WebView(this);
        GlobalFunctions.configureStandardWebView(dataSource);
        dataSource.setWebViewClient(new SearchWebClient(this));
        dataSource.addJavascriptInterface(new SearchJSInterface(this), "js_api");
        dataSource.loadUrl(GlobalConstants.URL_LIBRARY_HOME);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GlobalConstants.ACTIVITY_SEARCH_FILTER_REQUEST_CODE
                && resultCode == RESULT_OK && data != null){
            field = data.getIntExtra("field", 0);
            searchFilter = (ArrayList<Boolean>) data.getSerializableExtra("filters");
            searchLibrary = (ArrayList<Boolean>) data.getSerializableExtra("libraries");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_filter_changed_title);
            builder.setMessage(R.string.dialog_filter_changed_message);
            builder.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    performNewSearch(searchData);
                }
            });
            builder.setNegativeButton(R.string.dialog_button_no, null);
            builder.create().show();
        }
    }

    public String JSONSearchFilters(){
        JSONObject filter = new JSONObject();

        JSONArray filters = new JSONArray();
        for (Boolean b: searchFilter)
            filters.put(b);

        JSONArray libraries = new JSONArray();
        for (Boolean b : searchLibrary)
            libraries.put(b);

        try {
            filter.put("field", field);
            filter.put("filters", filters);
            filter.put("libraries", libraries);
        } catch (Exception e){
            return null;
        }
        return filter.toString();
    }

    private void requestNewSearch(){
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
                performNewSearch(input.getText().toString());
            }
        });
        alertBuilder.create().show();
    }

    private void performNewSearch(String query){
        if (query.isEmpty()) return;

        Intent newSearch = new Intent(this, SearchActivity.class);
        newSearch.putExtra("terms", query)
                .putExtra("field", field)
                .putExtra("filters", searchFilter)
                .putExtra("libraries", searchLibrary);
        startActivity(newSearch);
        finish();
    }

    public void setSearchResults(String results, boolean hasMore){
        if (results.isEmpty()) return;

        try {
            JSONArray jsResultsArr = new JSONArray(results);

            if (jsResultsArr.length() == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.dialog_ops_title);
                builder.setMessage(R.string.dialog_warning_message_no_results);
                builder.setPositiveButton(R.string.dialog_button_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestNewSearch();
                    }
                });
                builder.setNegativeButton(R.string.dialog_button_no, null);
                builder.create().show();
            }

            for (int i = 0; i < jsResultsArr.length(); ++i){
                JSONObject jsBook = jsResultsArr.getJSONObject(i);
                BookSearchProperties newBook = new BookSearchProperties();
                newBook.setTitle(jsBook.getString("title"));
                newBook.setAuthor(jsBook.getString("author"));
                newBook.setSection(jsBook.getString("section"));
                newBook.setType(jsBook.getString("type"));
                newBook.setCode(jsBook.getString("code"));
                availableBooks.add(newBook);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Snackbar.make(layout_holder,
                    R.string.snack_message_parse_fail, Snackbar.LENGTH_LONG)
                    .show();
        }

        if (availableBooks.size() > 10)
            Snackbar.make(findViewById(R.id.list_search_results),
                    R.string.snack_message_loaded_more_items,
                    Snackbar.LENGTH_LONG).show();

        fab_more.setVisibility(hasMore ? View.VISIBLE : View.GONE);
    }
}

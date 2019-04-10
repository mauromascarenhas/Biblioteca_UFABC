package com.nintersoft.bibliotecaufabc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;

public class SearchFiltersActivity extends AppCompatActivity {

    int searchField;

    Button toggle_filter;
    Spinner spinner_field;
    LinearLayout filter_layout;
    LinearLayout library_layout;
    ArrayList<Boolean> searchFilter;
    ArrayList<Boolean> searchLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_filters);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bindComponents();
        getCurrentFilters();
        setListeners();
    }


    private void bindComponents(){
        filter_layout = findViewById(R.id.filterOptions);
        library_layout = findViewById(R.id.libraryOptions);
        spinner_field = findViewById(R.id.spinner_field);
        toggle_filter = findViewById(R.id.button_toggle_filters);
        spinner_field.setAdapter(ArrayAdapter.createFromResource(this, R.array.search_options_field, android.R.layout.simple_spinner_dropdown_item));
    }

    @SuppressWarnings("unchecked")
    private void getCurrentFilters(){
        Intent sender = getIntent();
        searchField = sender.getIntExtra("field", 0);

        searchFilter = (ArrayList<Boolean>) sender.getSerializableExtra("filters");
        searchLibrary = (ArrayList<Boolean>) sender.getSerializableExtra("libraries");

        spinner_field.setSelection(searchField);

        String[] filters = getResources().getStringArray(R.array.search_options_types);
        String[] libraries = getResources().getStringArray(R.array.search_options_library_campus);

        for (int i = 0; i < searchFilter.size(); ++i){
            final int k = i;
            CheckBox cb = new CheckBox(this);
            cb.setChecked(searchFilter.get(i));
            cb.setText(filters[i]);
            cb.setTextSize(getResources().getDimension(R.dimen.label_form_item_def_size)/getResources().getDisplayMetrics().density);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    searchFilter.set(k, isChecked);
                }
            });

            filter_layout.addView(cb);
        }

        for (int i = 0; i < searchLibrary.size(); ++i){
            final int k = i;
            CheckBox cb = new CheckBox(this);
            cb.setChecked(searchLibrary.get(i));
            cb.setText(libraries[i]);
            cb.setTextSize(getResources().getDimension(R.dimen.label_form_item_def_size)/getResources().getDisplayMetrics().density);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    searchLibrary.set(k, isChecked);
                }
            });

            library_layout.addView(cb);
        }

    }

    private void setListeners(){
        toggle_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filter_layout.getVisibility() == View.VISIBLE)
                    filter_layout.setVisibility(View.GONE);
                else filter_layout.setVisibility(View.VISIBLE);
            }
        });
        spinner_field.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                searchField = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_filters, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                Intent result = new Intent();
                result.putExtra("field", searchField);
                result.putExtra("filters", searchFilter);
                result.putExtra("libraries", searchLibrary);
                setResult(RESULT_OK, result);
                finish();
                return true;
        }
        return super.onContextItemSelected(item);
    }
}

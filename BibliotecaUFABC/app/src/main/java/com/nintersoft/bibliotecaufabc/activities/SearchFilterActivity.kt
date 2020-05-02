package com.nintersoft.bibliotecaufabc.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import kotlinx.android.synthetic.main.activity_search_filter.*

class SearchFilterActivity : AppCompatActivity() {

    private var firstLibCheck : CheckBox? = null
    private var firstTypeCheck : CheckBox? = null

    private var filterField = 0
    private var filterType = arrayListOf<Boolean>()
    private var filterLibrary = arrayListOf<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_filter)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        defineCurrentFilters()
        bindComponents()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_filters, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_save -> {
                with (Intent()){
                    putExtra(Constants.SEARCH_FILTER_TYPE, filterType)
                    putExtra(Constants.SEARCH_FILTER_FIELD, filterField)
                    putExtra(Constants.SEARCH_FILTER_LIBRARY, filterLibrary)
                    setResult(Activity.RESULT_OK,this)
                }
                finish()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("UNCHECKED_CAST")
    private fun defineCurrentFilters(){
        filterField = intent.getIntExtra(Constants.SEARCH_FILTER_FIELD, 0)

        filterType = intent.getSerializableExtra(Constants.SEARCH_FILTER_TYPE) as ArrayList<Boolean>
        filterLibrary = intent.getSerializableExtra(Constants.SEARCH_FILTER_LIBRARY) as ArrayList<Boolean>

        spinner_field.adapter = ArrayAdapter.createFromResource(this,
            R.array.search_options_field, android.R.layout.simple_spinner_dropdown_item)
        spinner_field.setSelection(filterField)

        val types = resources.getStringArray(R.array.search_options_types)
        val libraries = resources.getStringArray(R.array.search_options_library_campus)

        for (i in 0 until filterType.size){
            val cb = CheckBox(this)
            cb.text = types[i]
            cb.isChecked = filterType[i]
            cb.textSize = resources.getDimension(R.dimen.label_form_item_def_size)/resources.displayMetrics.density
            cb.setOnCheckedChangeListener { _, isChecked ->
                filterType[i] = isChecked

                var hasChecked = false
                for (j in 1 until filterType.size) if (filterType[j]) hasChecked = true
                firstTypeCheck?.isChecked = !hasChecked
            }

            filterOptions.addView(cb)
            if (i == 0) firstTypeCheck = cb
        }

        for (i in 0 until filterLibrary.size){
            val cb = CheckBox(this)
            cb.text = libraries[i]
            cb.isChecked = filterLibrary[i]
            cb.textSize = resources.getDimension(R.dimen.label_form_item_def_size)/resources.displayMetrics.density
            cb.setOnCheckedChangeListener { _, isChecked ->
                filterLibrary[i] = isChecked

                var hasChecked = false
                for (j in 1 until filterLibrary.size) if (filterLibrary[j]) hasChecked = true
                firstLibCheck?.isChecked = !hasChecked
            }

            libraryOptions.addView(cb)
            if (i == 0) firstLibCheck = cb
        }
    }

    private fun bindComponents(){
        button_toggle_filters.setOnClickListener {
            filterOptions.visibility =
                if (filterOptions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        spinner_field.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterField = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }
}

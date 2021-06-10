package com.nintersoft.bibliotecaufabc.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.ui.loading.LoadingFragment
import com.nintersoft.bibliotecaufabc.ui.message.MessageFragment
import com.nintersoft.bibliotecaufabc.ui.message.MessageViewModel
import com.nintersoft.bibliotecaufabc.ui.search.SearchResultsFragment
import com.nintersoft.bibliotecaufabc.ui.search.SearchViewModel
import com.nintersoft.bibliotecaufabc.ui.snackbar.MessageSnackbar
import com.nintersoft.bibliotecaufabc.webclient.SearchWebClient
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.content_search.*
import org.json.JSONException
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {

    private var dataSource : WebView? = null
    private lateinit var searchViewModel : SearchViewModel
    private lateinit var messageViewModel : MessageViewModel

    @Suppress("UNCHECKED_CAST")
    private val openSearchFilterActivity = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()){ result ->
        if (result.resultCode == RESULT_OK && result.data != null){
            with(result.data!!){
                searchViewModel.setSearchField(getIntExtra(Constants.SEARCH_FILTER_FIELD, 0))
                searchViewModel.setSearchFilter(getSerializableExtra(Constants.SEARCH_FILTER_TYPE)
                        as ArrayList<Boolean>)
                searchViewModel.setLibraryFilters(getSerializableExtra(Constants.SEARCH_FILTER_LIBRARY)
                        as ArrayList<Boolean>)
            }

            AlertDialog.Builder(this).apply {
                setTitle(R.string.dialog_filter_changed_title)
                setMessage(R.string.dialog_filter_changed_message)
                setNegativeButton(R.string.dialog_button_no, null)
                setPositiveButton(R.string.dialog_button_yes, ({ _, _ ->
                    performNewSearch(searchViewModel.query.value!!)
                }))
            }.create().show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchViewModel = ViewModelProvider(this)[SearchViewModel::class.java]
        messageViewModel = ViewModelProvider(this)[MessageViewModel::class.java]

        initializeSearchData()
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        configureWebView()
        configureMessages()
        configureListeners()
        configureComponents()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configureWebView(){
        dataSource = WebView(this)
        Functions.configureWebView(dataSource!!, SearchWebClient(searchViewModel))
        dataSource?.loadUrl(Constants.URL_LIBRARY_HOME)
    }

    private fun configureMessages(){
        messageViewModel.setMessage(getString(R.string.lbl_book_details_connection_error))
        messageViewModel.setActLabel(getString(R.string.bt_error_message_try_again))
        messageViewModel.setAction { performNewSearch(searchViewModel.query.value!!) }
    }

    private fun configureListeners(){
        app_bar.setOnClickListener { requestNewSearch() }
        toolbar.setOnClickListener { requestNewSearch() }

        btn_filters.setOnClickListener{
            val filters = Intent(it.context, SearchFilterActivity::class.java)
            filters.putExtra(Constants.SEARCH_FILTER_TYPE, searchViewModel.searchFilter.value)
            filters.putExtra(Constants.SEARCH_FILTER_FIELD, searchViewModel.searchField.value!!)
            filters.putExtra(Constants.SEARCH_FILTER_LIBRARY, searchViewModel.searchLibrary.value)
            openSearchFilterActivity.launch(filters)
        }

        btn_more.setOnClickListener{
            dataSource?.evaluateJavascript("loadMoreBooks()", ({
                value: String? ->
                    when(value){
                        "null" -> requestNewChangeDetection()
                        else -> {
                            try { searchViewModel.addToSearchResults(JSONObject(value!!)) }
                            catch (_ : JSONException) { searchViewModel.setInternetError(true) }
                        }
                    }
            }))
            it.visibility = View.GONE
            MessageSnackbar.make(searchScroll, R.string.snack_message_loading_more_items,
                Snackbar.LENGTH_LONG)?.show()
        }

        var firstLoad = false
        searchViewModel.searchResults.observe(this, Observer {
            if (it == null) return@Observer
            if (!firstLoad) firstLoad = true
            else if (it.size > 10) MessageSnackbar.make(searchScroll,
                R.string.snack_message_loaded_more_items, Snackbar.LENGTH_LONG,
                MessageSnackbar.Type.INFO)?.show()
        })

        searchViewModel.hasNoResults.observe(this, Observer {
            if (it == null) return@Observer
            if (it) {
                AlertDialog.Builder(this).apply {
                    setTitle(R.string.dialog_ops_title)
                    setMessage(R.string.dialog_warning_message_no_results)
                    setNegativeButton(R.string.dialog_button_exit, ({ _, _ -> finish() }))
                    setPositiveButton(R.string.dialog_button_yes, ({ _, _ -> requestNewSearch() }))
                }.create().show()
            }
            else {
                val fragTransaction = supportFragmentManager.beginTransaction()
                fragTransaction.replace(R.id.searchScroll, SearchResultsFragment())
                fragTransaction.commitAllowingStateLoss()
            }
        })

        searchViewModel.hasMoreBooks.observe(this, {
            if (it == true) btn_more.show()
        })

        searchViewModel.internetError.observe(this, {
            if (it == true){
                val fragTransaction = supportFragmentManager.beginTransaction()
                fragTransaction.replace(R.id.searchScroll, MessageFragment())
                fragTransaction.commitAllowingStateLoss()
            }
        })
    }

    private fun configureComponents(){
        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.add(R.id.searchScroll, LoadingFragment())
        fragTransaction.commitAllowingStateLoss()
    }

    @Suppress("UNCHECKED_CAST")
    fun initializeSearchData(){
        searchViewModel.setSearchQuery(intent.getStringExtra(Constants.SEARCH_QUERY)!!)
        searchViewModel.setSearchField(intent.getIntExtra(Constants.SEARCH_FILTER_FIELD, 0))
        searchViewModel.setSearchFilter(intent.getSerializableExtra(Constants.SEARCH_FILTER_TYPE) as? ArrayList<Boolean>)
        searchViewModel.setLibraryFilters(intent.getSerializableExtra(Constants.SEARCH_FILTER_LIBRARY) as? ArrayList<Boolean>)

        toolbar.title = searchViewModel.query.value
    }

    private fun requestNewSearch(){
        val input = EditText(baseContext)
        input.inputType = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE.
            or(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT)
        input.imeOptions = EditorInfo.IME_ACTION_SEARCH
        input.setOnEditorActionListener { v, actionId, _ ->
            return@setOnEditorActionListener when (actionId){
                EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_DONE -> {
                    performNewSearch(v.text.toString())
                    true
                }
                else -> false
            }
        }

        val layout = LinearLayout(baseContext)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(resources.getDimensionPixelSize(R.dimen.dialog_view_margin), 0,
            resources.getDimensionPixelSize(R.dimen.dialog_view_margin), 0)
        input.layoutParams = params
        layout.addView(input)

        with (AlertDialog.Builder(this)){
            setTitle(R.string.dialog_input_title_search)
            setView(layout)
            setPositiveButton(R.string.dialog_button_search, ({
                    _ : DialogInterface, _ : Int ->
                performNewSearch(input.text.toString())
            }))
            .create().show()
        }
    }

    private fun performNewSearch(query : String){
        if (query.isEmpty()) return

        with (Intent(applicationContext, SearchActivity::class.java)){
            putExtra(Constants.SEARCH_QUERY, query)
            putExtra(Constants.SEARCH_FILTER_TYPE, searchViewModel.searchFilter.value)
            putExtra(Constants.SEARCH_FILTER_FIELD, searchViewModel.searchField.value)
            putExtra(Constants.SEARCH_FILTER_LIBRARY, searchViewModel.searchLibrary.value)
            startActivity(this)
        }
        finish()
    }

    private fun requestNewChangeDetection(){
        Handler(mainLooper).postDelayed(({
            dataSource?.evaluateJavascript("getServerChange();", ({
                if (it == "null") requestNewChangeDetection()
                else {
                    try { searchViewModel.addToSearchResults(JSONObject(it)) }
                    catch (_ : JSONException) { searchViewModel.setInternetError(true) }
                }
            }))
        }), 250)
    }
}

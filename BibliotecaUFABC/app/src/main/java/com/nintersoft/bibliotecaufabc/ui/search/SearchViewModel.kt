package com.nintersoft.bibliotecaufabc.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.model.AppContext
import com.nintersoft.bibliotecaufabc.model.search.BookSearch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SearchViewModel : ViewModel() {

    private val _query = MutableLiveData<String>().apply { value = "" }
    private val _searchField = MutableLiveData<Int>().apply { value = 0 }
    private val _searchFilter = MutableLiveData<ArrayList<Boolean>>().apply {
        value = arrayListOf(true)
        for (i in 1 until AppContext.context?.resources?.
                getStringArray(R.array.search_options_types)?.size!!) value?.add(false)
    }
    private val _searchLibrary = MutableLiveData<ArrayList<Boolean>>().apply {
        value = arrayListOf(true)
        for (i in 1 until AppContext.context?.resources?.
                getStringArray(R.array.search_options_library_campus)?.size!!) value?.add(false)
    }
    private val _searchResults = MutableLiveData<ArrayList<BookSearch>>().apply {
        value = arrayListOf()
    }
    private val _hasNoResults = MutableLiveData<Boolean?>().apply { value = null }
    private val _hasMoreBooks = MutableLiveData<Boolean?>().apply { value = null }
    private val _internetError = SingleLiveEvent<Boolean?>()

    fun setSearchQuery(query : String) { _query.value = query }
    fun setSearchField(value : Int){ _searchField.value = value }
    fun setSearchFilter(filters : List<Boolean>?){
        if (filters == null) return
        _searchFilter.value = ArrayList(filters)
    }
    fun setLibraryFilters(filters : List<Boolean>?){
        if (filters == null) return
        _searchLibrary.value = ArrayList(filters)
    }
    fun addToSearchResults(books : JSONObject?){
        if (books == null) return
        try {
            val jsBooksArr = books.getJSONArray("books")

            _hasNoResults.value = jsBooksArr.length() == 0

            val newResults = arrayListOf<BookSearch>().apply { addAll(_searchResults.value!!) }
            for (i in 0 until jsBooksArr.length()){
                newResults.add(BookSearch().apply {
                    val book = jsBooksArr.getJSONObject(i)
                    code = book.getString("code")
                    type = book.getString("type")
                    title = book.getString("title")
                    author = book.getString("author")
                    section = book.getString("section")
                })
            }
            _hasMoreBooks.value = books.getBoolean("hasMore")
            _searchResults.value = newResults
        }
        catch (_ : JSONException) { }
    }

    fun filtersAsJSON() : JSONObject {
        var filter = JSONObject()
        val filters = JSONArray()
        val libraries = JSONArray()

        _searchFilter.value?.forEach { filters.put(it) }
        _searchLibrary.value?.forEach { libraries.put(it) }

        try {
            filter.put("field", _searchField.value)
            filter.put("filters", filters)
            filter.put("libraries", libraries)
        } catch (_ : JSONException) { filter = JSONObject() }
        finally { return filter }
    }
    fun setInternetError(hasError: Boolean?){ _internetError.value = hasError }

    val query : LiveData<String> = _query
    val searchField : LiveData<Int> = _searchField
    val hasNoResults : LiveData<Boolean?> = _hasNoResults
    val hasMoreBooks : LiveData<Boolean?> = _hasMoreBooks
    val searchFilter : LiveData<ArrayList<Boolean>> = _searchFilter
    val searchLibrary : LiveData<ArrayList<Boolean>> = _searchLibrary
    val searchResults : LiveData<ArrayList<BookSearch>> = _searchResults
    val internetError : LiveData<Boolean?> = _internetError
}
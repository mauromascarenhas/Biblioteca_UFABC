package com.nintersoft.bibliotecaufabc.ui.search

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Functions
import kotlinx.android.synthetic.main.fragment_searchresults.*

class SearchResultsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_searchresults, container, false)
    }

    override fun onStart() {
        super.onStart()

        SearchViewAdapter(activity?.run {
            ViewModelProvider(this)[SearchViewModel::class.java]
        } ?: throw Exception("Invalid activity!")).also {
            searchResultsList.adapter = it
            it.clickedBook().observe(viewLifecycleOwner, Observer { book ->
                Functions.viewBookDetails(book, activity!!)
            })
            it.selectedBook().observe(viewLifecycleOwner, Observer {book ->
                AlertDialog.Builder(activity).apply {
                    setItems(R.array.book_item_context_menu,
                        ({ _, which ->
                            when (which) {
                                0 -> Functions.viewBookDetails(book, activity!!)
                                1 -> Functions.shareBookDetails(book, activity!!)
                            }
                        }))
                }.create().show()
            })
        }
        searchResultsList.layoutManager = LinearLayoutManager(context)
    }
}

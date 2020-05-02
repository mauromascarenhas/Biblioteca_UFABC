package com.nintersoft.bibliotecaufabc.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.activities.*
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private var hMenu: Menu? = null
    private var listener: HomeFragmentListener? = null
    private lateinit var homeViewModel: HomeViewModel

    interface HomeFragmentListener {
        fun onRequestRefresh()
        fun onRequestSignOut()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        configureObservers()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        super.onStart()
        HomeViewAdapter().also {
            homeBookList.adapter = it
            it.clickedBook().observe(viewLifecycleOwner, Observer {book ->
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
        homeBookList.layoutManager = LinearLayoutManager(context)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeFragmentListener) listener = context
        else throw RuntimeException("$context must implement HomeFragmentListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        hMenu = menu
        inflater.inflate(R.menu.home_frag_options_menu, menu)

        val searchView = SearchView((context as MainActivity).
            supportActionBar?.themedContext ?: context!!).apply {
            imeOptions = EditorInfo.IME_ACTION_SEARCH
        }
        menu.findItem(R.id.search).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)
            actionView = searchView
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val search = Intent(context, SearchActivity::class.java)
                search.putExtra(Constants.SEARCH_QUERY, query)
                (context as MainActivity).startActivity(search)
                menu.findItem(R.id.search).collapseActionView()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        homeViewModel.connectedUserName.value?.run {
            val connected = this != ""
            menu.findItem(R.id.login)?.isVisible = !connected
            hMenu?.findItem(R.id.sign_out)?.let {
                it.isVisible = connected
                if (connected) it.title = getString(R.string.menu_nav_sign_out,
                    homeViewModel.connectedUserName.value)
            }
        }

        menu.findItem(R.id.share).isVisible = PreferenceManager.
            getDefaultSharedPreferences(activity).getBoolean(
                getString(R.string.key_general_share_app_enabled), true)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.login -> activity?.startActivityForResult(Intent(context,
                LoginActivity::class.java), Constants.ACTIVITY_LOGIN_REQUEST_CODE)
            R.id.refresh -> listener?.onRequestRefresh()
            R.id.sign_out -> {
                item.isVisible = false
                listener?.onRequestSignOut()
            }
            R.id.settings -> startActivity(Intent(context, SettingsActivity::class.java))
            R.id.share -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_structure))
                }.also {
                    if (it.resolveActivity(activity!!.packageManager) != null)
                        activity?.startActivity(Intent.createChooser(it,
                            getString(R.string.intent_share_app)))
                }
            }
            R.id.exit -> activity?.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun configureObservers(){
        homeViewModel = activity?.run {
            ViewModelProvider(this)[HomeViewModel::class.java]
        } ?: throw Exception("Invalid activity")

        homeViewModel.connectedUserName.observe(viewLifecycleOwner, Observer {
            if (homeViewModel.loginStatus.value == null) return@Observer
            if (it == null) return@Observer
            else activity?.invalidateOptionsMenu()
        })
    }
}
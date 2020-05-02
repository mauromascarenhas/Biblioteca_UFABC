package com.nintersoft.bibliotecaufabc.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.ui.bookviewer.BookViewerFragment
import com.nintersoft.bibliotecaufabc.ui.bookviewer.BookViewerViewModel
import com.nintersoft.bibliotecaufabc.ui.loading.LoadingFragment
import com.nintersoft.bibliotecaufabc.ui.message.MessageFragment
import com.nintersoft.bibliotecaufabc.ui.message.MessageViewModel
import com.nintersoft.bibliotecaufabc.ui.search.SingleLiveEvent
import com.nintersoft.bibliotecaufabc.webclient.DetailsWebClient
import com.nintersoft.bibliotecaufabc.webclient.ReserveWebClient
import kotlinx.android.synthetic.main.activity_book_viewer.*

class BookViewerActivity : AppCompatActivity(), BookViewerFragment.ReservationEvents {

    companion object {
        val loggedInAs = SingleLiveEvent<String?>().apply { value = null }
    }

    private var dataSource: WebView? = null
    private var detailsWebClient: DetailsWebClient? = null
    private var reserveWebClient: ReserveWebClient? = null
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var bookViewerViewModel: BookViewerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_viewer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(supportFragmentManager.beginTransaction()){
            add(R.id.bookViewerParent, LoadingFragment())
            commitAllowingStateLoss()
        }

        messageViewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        bookViewerViewModel = ViewModelProvider(this)[BookViewerViewModel::class.java]
        initializeBookData()
        configureWebView()
        setListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.ACTIVITY_LOGIN_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK && data != null){
                detailsWebClient?.resetCounters()

                bookViewerViewModel.setLoginRequest(true)
                bookViewerViewModel.setReservationRequest(true)

                dataSource?.loadUrl(bookViewerViewModel.bookURL.value)
                Snackbar.make(bookViewerParent,
                    getString(R.string.snack_message_connected,
                        data.getStringExtra(Constants.CONNECTED_STATUS_USER_NAME)),
                    Snackbar.LENGTH_LONG).show()

                loggedInAs.value = data.getStringExtra(Constants.CONNECTED_STATUS_USER_NAME)
            }
            else bookViewerViewModel.setLoginCancelled(true)
        }
    }

    private fun initializeBookData(){
        var hasData = true
        var bookData = intent.getStringExtra(Constants.BOOK_CODE)
        if (bookData == null){
            if (intent.data != null){
                bookData = intent.data?.getQueryParameter(Constants.BOOK_QUERY_PARAMETER)
                hasData = bookData != null
            }
            else hasData = false
        }

        bookViewerViewModel.setBookURL(if (hasData) "${Constants.URL_LIBRARY_DETAILS}?codigo=$bookData" +
                Constants.MANDATORY_APPEND_URL_LIBRARY_DETAILS  else "")

        bookViewerViewModel.setLoginCancelled(false)
        bookViewerViewModel.setReservationRequest(false)
    }

    private fun configureLoginCanceledMessage(){
        messageViewModel.setActLabel(getString(R.string.menu_nav_refresh))
        messageViewModel.setMessage(getString(R.string.lbl_book_details_connection_canceled))
        messageViewModel.setAction(View.OnClickListener {
            with(supportFragmentManager.beginTransaction()){
                replace(R.id.bookViewerParent, LoadingFragment())
                commitAllowingStateLoss()
            }
            if (bookViewerViewModel.loginCancelled.value!!){
                startActivityForResult(Intent(this, LoginActivity::class.java),
                    Constants.ACTIVITY_LOGIN_REQUEST_CODE)
                bookViewerViewModel.setLoginCancelled(false)
            }
            else dataSource?.reload()
        })
    }

    private fun configureErrorMessage(){
        messageViewModel.setActLabel(getString(R.string.bt_error_message_try_again))
        messageViewModel.setMessage(getString(R.string.lbl_book_details_connection_error))
        messageViewModel.setAction(View.OnClickListener {
            with(supportFragmentManager.beginTransaction()){
                replace(R.id.bookViewerParent, LoadingFragment())
                commitAllowingStateLoss()
            }
            if (bookViewerViewModel.loginCancelled.value!!){
                startActivityForResult(Intent(this, LoginActivity::class.java),
                    Constants.ACTIVITY_LOGIN_REQUEST_CODE)
                bookViewerViewModel.setLoginCancelled(false)
            }
            else dataSource?.reload()
        })
    }

    private fun configureWebView(){
        dataSource = WebView(this)
        detailsWebClient = DetailsWebClient(bookViewerViewModel, messageViewModel)
        reserveWebClient = ReserveWebClient(bookViewerViewModel)
        Functions.configureWebView(dataSource!!, detailsWebClient!!)
        with (bookViewerViewModel.bookURL.value){
            if (!isNullOrEmpty()) dataSource?.loadUrl(this)
            else {
                AlertDialog.Builder(this@BookViewerActivity).apply {
                    setTitle(R.string.dialog_error_title)
                    setMessage(R.string.lbl_book_details_incorrect_data)
                    setNeutralButton(R.string.dialog_button_ok, ({ _, _ ->
                        this@BookViewerActivity.finish()
                    }))
                    setCancelable(false)
                }.create().show()
            }
        }
    }

    override fun reloadRequested() {
        with(supportFragmentManager.beginTransaction()){
            replace(R.id.bookViewerParent, LoadingFragment())
            commitAllowingStateLoss()
        }

        detailsWebClient?.resetCounters()
        dataSource?.webViewClient = detailsWebClient
        dataSource?.loadUrl(bookViewerViewModel.bookURL.value)
    }

    override fun requestReservation(options : String) {
        val script = "${Functions.scriptFromAssets("js/submit_reserve_scp.js")}\nsubmitReservationForm(\'$options\');"
        dataSource?.evaluateJavascript(script, ({
            if (it == "null") requestNewChangeDetection()
            else bookViewerViewModel.setReservationResult(it.substring(1, it.length - 1))
        }))
    }

    override fun reservationRequested() {
        reserveWebClient?.resetCounters()
        dataSource?.webViewClient = reserveWebClient
        dataSource?.evaluateJavascript("\nreserveBook();", null)
    }

    private fun setListeners(){
        bookViewerViewModel.bookProperties.observe(this, Observer {
            if (it == null) return@Observer
            else if (bookViewerViewModel.loginRequest.value!!){
                bookViewerViewModel.setLoginRequest(false)
                return@Observer
            }
            with(supportFragmentManager.beginTransaction()){
                replace(R.id.bookViewerParent, BookViewerFragment())
                commitAllowingStateLoss()
            }
        })
        bookViewerViewModel.dataSourceDetailsError.observe(this, Observer {
            if (it == true) {
                configureErrorMessage()
                with(supportFragmentManager.beginTransaction()){
                    replace(R.id.bookViewerParent, MessageFragment())
                    commitAllowingStateLoss()
                }
            }
        })

        bookViewerViewModel.loginCancelled.observe(this, Observer {
            if (it == true) {
                configureLoginCanceledMessage()
                with(supportFragmentManager.beginTransaction()){
                    replace(R.id.bookViewerParent, MessageFragment())
                    commitAllowingStateLoss()
                }
            }
        })
    }

    private fun requestNewChangeDetection(){
        Handler().postDelayed(({
            dataSource?.evaluateJavascript("getServerChange();", ({
                if (it == "null") requestNewChangeDetection()
                else bookViewerViewModel.setReservationResult(it.substring(1, it.length - 1))
            }))
        }), 250)
    }
}

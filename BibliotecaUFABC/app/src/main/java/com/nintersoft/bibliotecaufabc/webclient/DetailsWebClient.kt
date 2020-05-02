package com.nintersoft.bibliotecaufabc.webclient

import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.model.AppContext
import com.nintersoft.bibliotecaufabc.ui.bookviewer.BookViewerViewModel
import com.nintersoft.bibliotecaufabc.ui.message.MessageViewModel
import org.json.JSONException
import org.json.JSONObject

class DetailsWebClient(private val viewModel: BookViewerViewModel,
                        private val mViewModel: MessageViewModel) : WebViewClient(){

    private var bookPageFinished = 0

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { return true }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return true
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        if (bookPageFinished < 1){
            bookPageFinished++
            return
        }

        if (url?.contains(Constants.URL_LIBRARY_DETAILS)!!){
            val script = "${Functions.scriptFromAssets("js/details_scp.js")}\ngetBookDetails();"
            view?.evaluateJavascript(script, ({
                try { viewModel.setBookDetails(JSONObject(it)) }
                catch (_ : JSONException) {}
            }))
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                 error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        mViewModel.setMessage(AppContext.context!!.
            getString(R.string.lbl_book_details_connection_error))
        viewModel.setDataSourceDetailsError(true)
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?,
                                 failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        mViewModel.setMessage(AppContext.context!!.
            getString(R.string.lbl_book_details_connection_error))
        viewModel.setDataSourceDetailsError(true)
    }

    fun resetCounters(){ bookPageFinished = 0 }
}
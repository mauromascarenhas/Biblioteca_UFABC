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
import com.nintersoft.bibliotecaufabc.ui.renewals.RenewalsViewModel

class RenewalsWebClient(private val vModel : RenewalsViewModel) : WebViewClient() {

    private var confirmationPageFinished = 0

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { return true }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        if (url?.contains(Constants.URL_LIBRARY_PERFORM_RENEWAL) == true){
            if (confirmationPageFinished < 1){
                confirmationPageFinished++
                return
            }

            val script = "${Functions.scriptFromAssets("js/renewed_scp.js")}\ngetRenewalMessage();"
            view?.evaluateJavascript(script, ({ vModel.setRenewalMessage(it) }))
            confirmationPageFinished = 0
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                 error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        confirmationPageFinished = 0
        vModel.setNavError(AppContext.context!!.
            getString(R.string.lbl_book_details_connection_error))
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?,
                                 failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        confirmationPageFinished = 0
        vModel.setNavError(AppContext.context!!.
            getString(R.string.lbl_book_details_connection_error))
    }

}
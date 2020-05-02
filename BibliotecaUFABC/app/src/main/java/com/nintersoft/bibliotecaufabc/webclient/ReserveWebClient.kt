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
import org.json.JSONException
import org.json.JSONObject

class ReserveWebClient(private val vModel : BookViewerViewModel) : WebViewClient() {

    private val context = AppContext.context!!
    private var reservePageLoaded = 0

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { return false }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url.isNullOrEmpty()) return

        if (url.contains(Constants.URL_LIBRARY_RESERVE)){
            if (reservePageLoaded < 1){
                reservePageLoaded++
                return
            }

            val script = "${Functions.scriptFromAssets("js/reserve_scp.js")}\ndetectAction()"
            view?.evaluateJavascript(script, ({
                try {
                    with (JSONObject(it)){
                        if (getBoolean("hasData"))
                            vModel.setReservationAvailability(getJSONObject("data"))
                        else vModel.setReservationResult(getString("message"))
                    }
                } catch (error : JSONException) {  vModel.setReservationResult(context.
                        getString(R.string.dialog_reserve_parse_error)) }
            }))
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                 error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        vModel.setReservationError(AppContext.context?.
            getString(R.string.lbl_book_details_connection_error))
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?,
                                 failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        vModel.setReservationError(AppContext.context?.
            getString(R.string.lbl_book_details_connection_error))
    }

    fun resetCounters(){ reservePageLoaded = 0 }

}
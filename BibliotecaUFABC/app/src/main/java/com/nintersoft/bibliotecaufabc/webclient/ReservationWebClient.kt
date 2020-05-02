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
import com.nintersoft.bibliotecaufabc.ui.reservations.ReservationsViewModel
import org.json.JSONException
import org.json.JSONObject

class ReservationWebClient(private val viewModel : ReservationsViewModel)
    : WebViewClient() {

    private val context = AppContext.context
    private var reservationPageFinished = 0
    private var confirmationPageFinished = 0

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { return false }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url.isNullOrEmpty()) return

        if (url.contains(Constants.URL_LIBRARY_CANCEL_RESERVATION)){
            if (confirmationPageFinished < 1){
                confirmationPageFinished++
                return
            }

            val script = Functions.scriptFromAssets("js/reservation_scp.js") + "\ngetCancellationMessage();"
            view?.evaluateJavascript(script, ({
                viewModel.setCancellationMessage(it)
                viewModel.requestReload(true)
            }))
            reservationPageFinished = 0
        }
        else if (url == Constants.URL_LIBRARY_RESERVATION){
            if (reservationPageFinished < 1){
                reservationPageFinished++
                return
            }

            val script = Functions.scriptFromAssets("js/reservation_scp.js") + "\ngetReservations();"
            view?.evaluateJavascript(script, ({
                try{
                    with(JSONObject(it)){
                        if (getBoolean("hasErrorDiv"))
                            viewModel.setUserName(getString("username"))
                        else {
                            viewModel.setUserName(null)
                            viewModel.setReservedBooks(getJSONArray("reservationBooks"))
                        }
                    }
                }
                catch (_ : JSONException){ viewModel.setUserName(context?.
                    getString(R.string.on_error_def_username)) }
                finally { reservationPageFinished = 0 }
            }))
            confirmationPageFinished = 0
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                 error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        reservationPageFinished = 0
        confirmationPageFinished = 0
        viewModel.setLoadError(true)
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?,
        failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        reservationPageFinished = 0
        confirmationPageFinished = 0
        viewModel.setLoadError(true)
    }

}
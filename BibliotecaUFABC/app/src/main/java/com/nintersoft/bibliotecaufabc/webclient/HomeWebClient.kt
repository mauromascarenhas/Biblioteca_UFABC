package com.nintersoft.bibliotecaufabc.webclient

import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.ui.home.HomeViewModel
import org.json.JSONException
import org.json.JSONObject

class HomeWebClient(private val homeViewModel : HomeViewModel) : WebViewClient() {

    private var homePageLoaded = 0

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { return false }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url?.contains(Constants.URL_LIBRARY_NEWEST) != true) return
        if (homePageLoaded < 1){
            homePageLoaded++
            return
        }

        var script = "${Functions.scriptFromAssets("js/main_scp.js")}\ncheckLoginStatus();"
        view?.evaluateJavascript(script, ({
            try {
                val result = JSONObject(it)
                homeViewModel.defineLoginStatus(result.getBoolean("status"))
                homeViewModel.defineConnectedUserName(result.getString("name"))
            } catch (e : JSONException) {
                homeViewModel.defineLoginStatus(false)
                homeViewModel.defineConnectedUserName("")
            }
        }))

        script = "${Functions.scriptFromAssets("js/main_scp.js")}\ngetNewestBooks();"
        view?.evaluateJavascript(script, ({ homeViewModel.defineBookSearchResults(it) }))
        homePageLoaded = 0
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                 error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        homePageLoaded = 0
        homeViewModel.defineLoadError(true)
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?,
                                 failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        homePageLoaded = 0
        homeViewModel.defineLoadError(true)
    }
}
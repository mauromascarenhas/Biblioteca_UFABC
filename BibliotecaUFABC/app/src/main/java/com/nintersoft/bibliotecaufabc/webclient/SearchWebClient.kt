package com.nintersoft.bibliotecaufabc.webclient

import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.ui.search.SearchViewModel
import org.json.JSONException
import org.json.JSONObject

class SearchWebClient(private val vModel: SearchViewModel) : WebViewClient() {

    private var searchHomeFinished = 0
    private var searchSearchFinished = 0

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { return false }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        if (url!!.contains(Constants.URL_LIBRARY_HOME)){
            if (searchHomeFinished == 0){
                searchHomeFinished++
                return
            }

            val script = Functions.scriptFromAssets("js/search_scp.js") +
                    "\ndocumentReady();" +
                    "\nperformSearch(\"${vModel.query.value}\", \'${vModel.filtersAsJSON()}\');"
            view?.evaluateJavascript(script, null)
            searchHomeFinished++
        }
        else if (url.contains(Constants.URL_LIBRARY_SEARCH)){
            if (searchSearchFinished > 0) return
            searchSearchFinished++

            val script = "${Functions.scriptFromAssets("js/search_scp.js")}\ngetSearchResults();"
            view?.evaluateJavascript(script, ({
                try { vModel.addToSearchResults(JSONObject(it)) }
                catch (_ : JSONException){ vModel.setInternetError(true) }
            }))
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        vModel.setInternetError(true)
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        vModel.setInternetError(true)
    }

}
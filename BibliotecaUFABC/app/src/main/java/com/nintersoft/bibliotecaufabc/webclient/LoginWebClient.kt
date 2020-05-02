package com.nintersoft.bibliotecaufabc.webclient

import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.model.AppContext
import com.nintersoft.bibliotecaufabc.ui.search.SingleLiveEvent


class LoginWebClient : WebViewClient() {

    private val mContext = AppContext.context!!
    private var loginPageFinished = 0
    private var loginServicesFinished = 0
    private val _error = SingleLiveEvent<Boolean?>()
    private val _hasLoggedIn = SingleLiveEvent<String?>()
    private val _loginRequest = SingleLiveEvent<Boolean?>()

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { return false }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url.isNullOrEmpty()) return

        if (url.contains(Constants.URL_LIBRARY_LOGIN_P) && loginPageFinished == 0){
            _loginRequest.value = PreferenceManager.getDefaultSharedPreferences(mContext).
                getBoolean(mContext.getString(R.string.key_privacy_store_password), true)
            loginPageFinished++
        }
        else if (url.contains(Constants.URL_LIBRARY_SERVICES)){
            if (loginServicesFinished < 1){
                loginServicesFinished++
                return
            }

            val script = "${Functions.scriptFromAssets("js/login_scp.js")} \ngetUsername();"
            view?.evaluateJavascript(script, ({ _hasLoggedIn.value = it }))
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        loginPageFinished = 0
        loginServicesFinished = 0
        _error.value = true
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        loginPageFinished = 0
        loginServicesFinished = 0
        _error.value = true
    }

    fun errorEvent() : LiveData<Boolean?>{ return _error }
    fun loginStatus() : LiveData<String?> { return _hasLoggedIn }
    fun loginRequested() : LiveData<Boolean?> { return _loginRequest }
}
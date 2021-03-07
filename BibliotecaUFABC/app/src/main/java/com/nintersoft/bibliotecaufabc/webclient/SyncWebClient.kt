package com.nintersoft.bibliotecaufabc.webclient

import android.content.Context
import android.os.Build
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.model.AppDatabase
import com.nintersoft.bibliotecaufabc.model.renewal.BookRenewal
import com.nintersoft.bibliotecaufabc.synchronization.SyncService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SyncWebClient(private val context : Context)
    : WebViewClient() {

    private var loginPageFinished = 0
    private var loginServicesFinished = 0
    private var renewalPageFinished = 0

    private var uLogin : String? = null
    private var uPassword : String? = null

    init {
        PreferenceManager.getDefaultSharedPreferences(context).let {
            uLogin = it.getString(context.
                getString(R.string.key_privacy_login_username), "")
            uPassword = it.getString(context.
                getString(R.string.key_privacy_login_password), "")

            if (!it.getBoolean(context
                    .getString(R.string.key_privacy_store_password), true) ||
                    uLogin.isNullOrEmpty() || uPassword.isNullOrEmpty())
                (context as SyncService).setStatus(SyncService.Companion.LStatus.FINISHED_SUCCESS)
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean { return false }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (url.isNullOrEmpty()) return

        when {
            url.contains(Constants.URL_LIBRARY_LOGIN_P) -> {
                if (loginPageFinished == 0) {
                    loginPageFinished++
                    return
                }

                renewalPageFinished = 0
                loginServicesFinished = 0
                val script = "${Functions.scriptFromAssets("js/login_scp.js")}\nperformLogin('$uLogin','$uPassword');"
                view?.evaluateJavascript(script, null)

                with (context as SyncService) {
                    scheduleChecking()
                    killLater()
                }
            }
            url.contains(Constants.URL_LIBRARY_SERVICES) -> {
                if (loginServicesFinished < 1){
                    loginServicesFinished++
                    return
                }

                renewalPageFinished = 0
                view?.loadUrl(Constants.URL_LIBRARY_RENEWAL)
            }
            url.contains(Constants.URL_LIBRARY_RENEWAL) -> {
                if (renewalPageFinished < 1){
                    renewalPageFinished++
                    return
                }

                loginPageFinished = 0
                loginServicesFinished = 0

                val script = "${Functions.scriptFromAssets("js/renewal_scp.js")}\ngetRenewals();"
                view?.evaluateJavascript(script, ({result ->
                    try {
                        with (JSONObject(result)) {
                            if (getBoolean("connected"))
                                setReservationBooks(if (getBoolean("hasErrorDiv"))
                                    JSONArray() else getJSONArray("renewalBooks"))
                            else view.loadUrl(Constants.URL_LIBRARY_LOGIN_P)
                        }
                    } catch (_ : JSONException) { (context as SyncService).setStatus(SyncService.Companion.LStatus.FINISHED_FAILURE) }
                }))
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
        error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        (context as SyncService).setStatus(SyncService.Companion.LStatus.FINISHED_FAILURE)
    }

    @Suppress("DEPRECATION")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?,
                                 failingUrl: String?) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        (context as SyncService).setStatus(SyncService.Companion.LStatus.FINISHED_FAILURE)
    }

    private fun setReservationBooks(jsBooks : JSONArray){
        try {
            bindAlarms(arrayListOf<BookRenewal>().also {list ->
                for (i in 0 until jsBooks.length()){
                    list.add(BookRenewal().also {book ->
                        with (jsBooks.getJSONObject(i)){
                            book.id = i.toLong()
                            book.title = getString("title")
                            book.library = getString("library")
                            book.patrimony = getString("patrimony")
                            book.date = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).
                                parse(getString("date").split(" ")[2])
                            book.renewalLink = getString("renewal_link")
                        }
                    })
                }
            })
            (context as SyncService).setStatus(SyncService.Companion.LStatus.FINISHED_SUCCESS)
        } catch (_ : JSONException) { (context as SyncService).setStatus(SyncService.Companion.LStatus.FINISHED_FAILURE) }
    }

    private fun bindAlarms(books : List<BookRenewal>){
        AppDatabase.getInstance()?.bookRenewalDAO()?.let {dao ->
            GlobalScope.launch {
                dao.removeAll()
                books.forEach { dao.insert(it) }
            }
            Functions.scheduleRenewalAlarms()

            PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putLong(context.getString(R.string.key_synchronization_schedule),
                    System.currentTimeMillis()).apply()
        }

    }
}
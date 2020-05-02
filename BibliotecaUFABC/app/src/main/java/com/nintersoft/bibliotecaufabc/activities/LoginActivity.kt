package com.nintersoft.bibliotecaufabc.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.nintersoft.bibliotecaufabc.ui.loginform.LoginForm
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.ui.loading.LoadingFragment
import com.nintersoft.bibliotecaufabc.webclient.LoginWebClient
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), LoginForm.OnFragmentInteractionListener{

    private var login : String? = null
    private var password : String? = null
    private var dataSource : WebView? = null
    private val errorHandler = Handler()
    private val errorChecker = {
        if (dataSource!!.url.contains(Constants.URL_LIBRARY_LOGIN_P)) checkForErrors()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.add(R.id.loginViewGroup, LoadingFragment())
        fragTransaction.commitAllowingStateLoss()
    }

    override fun onStart() {
        super.onStart()
        configureWebView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onLoginRequest(username: String, password: String) {
        val imm = (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)
        if (imm != null && currentFocus != null)
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)

        if ((username.isEmpty() || password.isEmpty())) {
            val form = LoginForm()
            with(Bundle()){
                putString(Constants.LOGIN_TO_FORM_USERNAME, username)
                putString(Constants.LOGIN_TO_FORM_PASSWORD, password)
                if (username.isEmpty()) putString(Constants.LOGIN_TO_FORM_USERNAME_ERROR, getString(R.string.empty_edt_form))
                if (password.isEmpty()) putString(Constants.LOGIN_TO_FORM_PASSWORD_ERROR, getString(R.string.empty_edt_form))
                form.arguments = this
            }

            val fragTransaction = supportFragmentManager.beginTransaction()
            fragTransaction.replace(R.id.loginViewGroup, form)
            fragTransaction.addToBackStack(null)
            fragTransaction.commitAllowingStateLoss()
            return
        }

        this.login = username
        this.password = password

        val script = "${Functions.scriptFromAssets("js/login_scp.js")} \nperformLogin('$username', '$password');"
        dataSource?.evaluateJavascript(script, null)

        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.replace(R.id.loginViewGroup, LoadingFragment())
        fragTransaction.addToBackStack(null)
        fragTransaction.commitAllowingStateLoss()

        PreferenceManager.getDefaultSharedPreferences(applicationContext).let {prefs ->
            if (prefs.getBoolean(getString(R.string.key_privacy_store_password), true)){
                prefs.edit().also {
                    it.putString(getString(R.string.key_privacy_login_username), username)
                    it.putString(getString(R.string.key_privacy_login_password), password)
                }.apply()
            }
        }

        errorHandler.postDelayed(errorChecker, 1000)
    }

    private fun configureWebView(){
        dataSource = WebView(this)
        Functions.configureWebView(dataSource!!, LoginWebClient().also {lwc ->
            lwc.errorEvent().observe(this, Observer {
                if (it == true) displayErrorMessage()
            })
            lwc.loginStatus().observe(this, Observer {userName ->
                errorHandler.removeCallbacks(errorChecker)

                with (Intent()){
                    putExtra(Constants.CONNECTED_STATUS_USER_NAME,
                        userName?.replace("\"", ""))
                    setResult(Activity.RESULT_OK, this)
                }
                finish()
            })
            lwc.loginRequested().observe(this, Observer {
                when (it){
                    true -> setSavedUserLogin()
                    false -> setupInterface()
                }
            })
        })
        dataSource?.loadUrl(Constants.URL_LIBRARY_LOGIN_P)
    }

    private fun setSavedUserLogin(){
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        login = pref.getString(getString(R.string.key_privacy_login_username), "")
        password = pref.getString(getString(R.string.key_privacy_login_password), "")

        if (login.isNullOrEmpty() || password.isNullOrEmpty()){
            setupInterface()
            return
        }
        onLoginRequest(login!!, password!!)
    }

    private fun setupInterface(){
        val fragTransaction = supportFragmentManager.beginTransaction()
        fragTransaction.replace(R.id.loginViewGroup, LoginForm())
        fragTransaction.addToBackStack(null)
        fragTransaction.commitAllowingStateLoss()
    }

    private fun displayErrorMessage(){
        val ab = AlertDialog.Builder(this)
        ab.setMessage(R.string.dialog_message_login_error)
        ab.setNegativeButton(R.string.dialog_button_no, ({ _, _ -> finish() }))
        ab.setPositiveButton(R.string.dialog_button_yes, ({ _, _ ->
            dataSource?.loadUrl(Constants.URL_LIBRARY_LOGIN_P)
        }))
        ab.create().show()
    }

    private fun checkForErrors(){
        val script = "${Functions.scriptFromAssets("js/login_scp.js")} \ncheckForErrors();"
        dataSource?.evaluateJavascript(script, ({
            try {
                val result = JSONObject(it)
                if (result.getBoolean("hasFormError")) {
                    val loginFragment = LoginForm()
                    with(Bundle()){
                        putString("username", login)
                        putString("password", password)
                        loginFragment.arguments = this
                    }

                    val fragTransaction = supportFragmentManager.beginTransaction()
                    fragTransaction.replace(R.id.loginViewGroup, loginFragment)
                    fragTransaction.addToBackStack(null)
                    fragTransaction.commitAllowingStateLoss()

                    val ab = AlertDialog.Builder(this@LoginActivity)
                    ab.setMessage(result.getString("errorDetails"))
                    ab.setPositiveButton(R.string.dialog_button_ok, null)
                    ab.create().show()
                }
                else errorHandler.postDelayed(errorChecker, 250)
            } catch (_ : JSONException) { displayErrorMessage() }
        }))
    }
}

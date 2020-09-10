package com.nintersoft.bibliotecaufabc.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.synchronization.SyncService
import com.nintersoft.bibliotecaufabc.ui.home.HomeFragment
import com.nintersoft.bibliotecaufabc.ui.home.HomeViewModel
import com.nintersoft.bibliotecaufabc.ui.reservations.ReservationsRecyclerFragment
import com.nintersoft.bibliotecaufabc.ui.reservations.ReservationsViewModel
import com.nintersoft.bibliotecaufabc.ui.snackbar.MessageSnackbar
import com.nintersoft.bibliotecaufabc.webclient.HomeWebClient
import com.nintersoft.bibliotecaufabc.webclient.ReservationWebClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ReservationsRecyclerFragment.ReservationEvents,
    HomeFragment.HomeFragmentListener {

    private var dataSource : WebView? = null
    private var reservationSource : WebView? = null

    private lateinit var prefs : SharedPreferences
    private lateinit var loadingAlert : AlertDialog
    private lateinit var homeViewModel : HomeViewModel
    private lateinit var reservationsViewModel : ReservationsViewModel

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if(key == getString(R.string.key_general_share_app_enabled)) invalidateOptionsMenu()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        reservationsViewModel = ViewModelProvider(this)[ReservationsViewModel::class.java]

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_renewal,
                R.id.navigation_reservation
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        Functions.createNotificationChannel(
            getString(R.string.notification_sync_channel_description),
            getString(R.string.notification_sync_channel_description),
            Constants.CHANNEL_SYNC_ID
        )
        Functions.createNotificationChannel(
            getString(R.string.notification_renewal_channel_title),
            getString(R.string.notification_renewal_channel_description),
            Constants.CHANNEL_RENEWAL_ID
        )

        configureWebView()
        configureObservers()
        prefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    override fun onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        if (!prefs.getBoolean(getString(R.string.key_privacy_cache_main_content), true)){
            GlobalScope.launch { Glide.get(applicationContext).clearDiskCache() }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            }
            dataSource?.clearCache(true)
            dataSource?.clearFormData()
            dataSource?.clearHistory()
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            Constants.ACTIVITY_LOGIN_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK){
                    val user = data?.getStringExtra(Constants.CONNECTED_STATUS_USER_NAME)
                    if (!user.isNullOrEmpty()){
                        homeViewModel.defineLoginStatus(true)
                        homeViewModel.defineConnectedUserName(user)
                    }
                }
            }
            Constants.SYNC_PERMISSION_REQUEST_ID -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (!Settings.canDrawOverlays(this))
                        AlertDialog.Builder(this).apply {
                            setTitle(R.string.notification_sync_denied_title)
                            setMessage(R.string.notification_sync_denied_message)
                            setPositiveButton(R.string.dialog_button_ok, null)
                        }.create().show()
                    else {
                        startPowerSavingSettings()
                        setSyncSchedule()
                    }
                }
            }
        }
    }

    override fun onRequestRefresh() {
        dataSource?.loadUrl(Constants.URL_LIBRARY_NEWEST)
        MessageSnackbar.make(container, R.string.snack_message_refreshing_newest,
            Snackbar.LENGTH_SHORT)?.setAnchorView(nav_view)?.show()
    }

    override fun onRequestSignOut() {
        dataSource?.loadUrl(Constants.URL_LIBRARY_LOGOUT)
        homeViewModel.askedLogout.value = true
        homeViewModel.defineLoginStatus(false)
        homeViewModel.defineConnectedUserName("")
        invalidateOptionsMenu()
    }

    override fun loadCancelLink(link: String) { reservationSource?.loadUrl(link) }

    override fun refreshReservations() { reservationSource?.
        loadUrl(Constants.URL_LIBRARY_RESERVATION) }

    private fun configureWebView(){
        dataSource = WebView(this)
        reservationSource = WebView(this)
        Functions.configureWebView(dataSource!!, HomeWebClient(homeViewModel))
        Functions.configureWebView(reservationSource!!,
            ReservationWebClient(reservationsViewModel))

        loadingAlert = AlertDialog.Builder(this).apply {
            setView(R.layout.message_progress_dialog)
            setOnCancelListener {
                MessageSnackbar.make(container, R.string.snack_message_loading_newest,
                    Snackbar.LENGTH_SHORT)?.setAnchorView(nav_view)?.show()
            }
        }.create()
        loadingAlert.show()
    }

    private fun configureObservers(){
        homeViewModel.loginStatus.observe(this, {
            when (it){
                null -> dataSource?.loadUrl(Constants.URL_LIBRARY_NEWEST)
                true -> {
                    if (loadingAlert.isShowing) loadingAlert.dismiss()
                    reservationSource?.loadUrl(Constants.URL_LIBRARY_RESERVATION)
                    homeViewModel.askedLogout.value = false
                    homeViewModel.hasCheckedPermission.let {has ->
                        if (!has.value!!) {
                            requestSyncPermission()
                            has.value = true
                        }
                    }
                }
                else -> {
                    if (loadingAlert.isShowing) loadingAlert.dismiss()
                    if (prefs.getBoolean(getString(R.string.key_privacy_auto_login), true) &&
                            !homeViewModel.hasRequestedLogin.value!!) {
                        startActivityForResult(
                            Intent(this, LoginActivity::class.java),
                            Constants.ACTIVITY_LOGIN_REQUEST_CODE
                        )
                        homeViewModel.hasRequestedLogin.value = true
                    }
                }
            }
            nav_view.menu.findItem(R.id.navigation_reservation).isEnabled = it == true
        })

        homeViewModel.dataLoadError.observe(this, Observer {
            if (it == null) return@Observer
            if (loadingAlert.isShowing) loadingAlert.dismiss()
            MessageSnackbar.make(container, getString(R.string.snack_message_loading_main_error),
                Snackbar.LENGTH_LONG, MessageSnackbar.Type.ERROR)?.setAnchorView(nav_view)?.apply {
                setAction(R.string.snack_button_reload, ({
                    dataSource?.loadUrl(Constants.URL_LIBRARY_NEWEST)
                    Snackbar.make(container, R.string.snack_message_loading_newest,
                        Snackbar.LENGTH_SHORT).setAnchorView(nav_view).show()
                }))
            }?.show()
        })

        homeViewModel.connectedUserName.observe(this, Observer {
            if (it.isNullOrEmpty()) return@Observer
            homeViewModel.hasRequestedLogin.value = true
            MessageSnackbar.make(container, getString(R.string.snack_message_connected, it),
                Snackbar.LENGTH_LONG, MessageSnackbar.Type.INFO)?.setAnchorView(nav_view)?.show()
        })

        reservationsViewModel.loadError.observe(this, {
            if (it == true) MessageSnackbar.make(container,
                getString(R.string.snack_message_loading_reservation_error),
                Snackbar.LENGTH_LONG, MessageSnackbar.Type.ERROR)?.setAnchorView(nav_view)?.show()
        })

        BookViewerActivity.loggedInAs.observe(this, {
            if (!it.isNullOrEmpty()){
                homeViewModel.defineLoginStatus(true)
                homeViewModel.defineConnectedUserName(it)
            }
        })
    }

    @Synchronized
    private fun requestSyncPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)){
            prefs.edit().putBoolean(getString(R.string.key_app_first_run), true).apply()

            AlertDialog.Builder(this).apply {
                setTitle(R.string.notification_sync_rationale_title)
                setMessage(R.string.notification_sync_rationale_message)
                setPositiveButton(R.string.dialog_button_ok, ({ dlg, _ ->
                    dlg.cancel()
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")).also {
                        if (it.resolveActivity(packageManager) != null)
                            startActivityForResult(it, Constants.SYNC_PERMISSION_REQUEST_ID)
                    }
                }))
            }.create().show()
        }
        else if (!homeViewModel.hasRequestedSync.value!!) {
            setSyncSchedule()

            if (SyncService.isRunning.value == false)
                ContextCompat.startForegroundService(this,
                    Intent(this, SyncService::class.java).
                        putExtra(Constants.SYNC_INTENT_SCHEDULED, false))
            homeViewModel.hasRequestedSync.value = true
        }
    }

    private fun setSyncSchedule(force : Boolean = false){
        val workState = WorkManager.getInstance(applicationContext).
            getWorkInfosForUniqueWork(Constants.WORK_SYNC_SERVICE_WORKER).let {
            try {
                if (it.get().size == 0) WorkInfo.State.CANCELLED
                else it.get()[0].state
            } catch (_ : ExecutionException) { WorkInfo.State.CANCELLED }
            catch (_ : InterruptedException) { WorkInfo.State.CANCELLED }
        }
        if (prefs.getBoolean(getString(R.string.key_app_first_run), true) || force ||
            ((workState != WorkInfo.State.RUNNING && workState != WorkInfo.State.ENQUEUED)
                    && prefs.getBoolean(getString(R.string.key_notification_enable_warning),
                        true))){
            Functions.schedulePeriodicSync(TimeUnit.DAYS.toMillis(prefs.
                getString(getString(R.string.key_notification_sync_interval), "2")!!.toLong()),
                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS)
            Functions.schedulePeriodicSyncReminder(TimeUnit.DAYS.toMillis(Constants.
                SYNC_REMINDER_NOTIFICATION_INTERVAL), PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
                TimeUnit.DAYS.toMillis(1))
            prefs.edit().putBoolean(getString(R.string.key_app_first_run), false).apply()
        }
    }

    private fun startPowerSavingSettings(){
        Constants.POWER_MANAGER_INTENTS.forEach {
            if (Functions.isCallable(it)){
                AlertDialog.Builder(this).apply {
                    setTitle(getString(R.string.dialog_permission_title, Build.MANUFACTURER))
                    setMessage(getString(R.string.dialog_permission_message,
                        getString(R.string.app_name)))
                    setPositiveButton(R.string.dialog_button_yes, ({ _, _ ->
                        if (Build.MANUFACTURER.toLowerCase(Locale.getDefault()) == "xiaomi"){
                            try {
                                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).
                                    setData(Uri.parse("package:$packageName")))
                            } catch (_ : Exception){ startActivity(it) }
                        }
                        else startActivity(it)
                    }))
                    setNegativeButton(R.string.dialog_button_no, null)
                }.create().show()
                return@forEach
            }
        }
    }
}

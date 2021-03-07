package com.nintersoft.bibliotecaufabc.synchronization

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import com.nintersoft.bibliotecaufabc.webclient.SyncWebClient
import org.json.JSONException
import org.json.JSONObject

class SyncService : Service() {

    @Suppress("ObjectPropertyName")
    companion object {
        enum class LStatus {
            FINISHED_SUCCESS,
            FINISHED_FAILURE,
            RUNNING,
            STOPPED
        }

        private val _status = MutableLiveData<LStatus>().apply { value = LStatus.STOPPED }
        val status : LiveData<LStatus> = _status
    }

    private var dataSource : WebView? = null
    private var windowManager : WindowManager? = null
    private var isScheduled = true

    private val selfObs : Observer<LStatus> = Observer { st ->
        when (st) {
            LStatus.FINISHED_FAILURE, LStatus.FINISHED_SUCCESS -> finish()
            else -> return@Observer
        }
    }

    private var mHandler : Handler? = null
    private val killService = Runnable { _status.value = LStatus.FINISHED_FAILURE }
    private val errorChecker = Runnable {
        if (dataSource?.url?.contains(Constants.URL_LIBRARY_LOGIN_P) == true)
            checkForErrors()
    }

    override fun onBind(intent: Intent): IBinder? { return null }

    override fun onCreate() {
        super.onCreate()

        mHandler = Handler(mainLooper)

        Functions.createNotificationChannel(
            getString(R.string.notification_sync_channel_title),
            getString(R.string.notification_sync_channel_description),
            Constants.CHANNEL_SYNC_ID
        )
        Functions.createNotificationChannel(
            getString(R.string.notification_renewal_channel_title),
            getString(R.string.notification_renewal_channel_description),
            Constants.CHANNEL_RENEWAL_ID
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)){
            Functions.createSyncNotification(R.string.notification_sync_removed_title,
                R.string.notification_sync_removed_message,
                Constants.SYNC_NOTIFICATION_REVOKED_ID)
            Functions.cancelPeriodicSync()
            return
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        @Suppress("DEPRECATION")
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
            height = 0
            width = 0
            x = 0
            y = 0
        }

        dataSource = WebView(this)
        Functions.configureWebView(dataSource!!, SyncWebClient(this))
        windowManager?.addView(dataSource, params)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        _status.value = LStatus.RUNNING
        _status.observeForever(selfObs)
        return if (dataSource == null) {
            stopSelf()
            START_NOT_STICKY
        } else {
            isScheduled = intent?.
            getBooleanExtra(Constants.SYNC_INTENT_SCHEDULED, true) ?: true
            startForeground(Constants.SYNC_NOTIFICATION_ID, createSyncNotification())
            dataSource?.loadUrl(Constants.URL_LIBRARY_RENEWAL)
            super.onStartCommand(intent, flags, startId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _status.value = LStatus.STOPPED
        if (dataSource != null) windowManager?.removeView(dataSource)
        mHandler?.removeCallbacks(killService)
    }

    fun killLater(){ mHandler?.postDelayed(killService, 180000) }

    fun scheduleChecking(){ mHandler?.postDelayed(errorChecker, 1000) }

    private fun checkForErrors(){
        val script = "${Functions.scriptFromAssets("js/login_scp.js")}\ncheckForErrors();"
        dataSource?.evaluateJavascript(script, ({
            try{
                if (JSONObject(it).getBoolean("hasFormError")){
                    Functions.createSyncNotification(R.string.notification_sync_error_title,
                        R.string.notification_sync_error_message,
                        Constants.SYNC_NOTIFICATION_UPDATE_ID)
                    finish()
                }
                else mHandler?.postDelayed(errorChecker, 250)
            } catch (_ : JSONException) { _status.value = LStatus.FINISHED_FAILURE }
        }))
    }

    private fun createSyncNotification() : Notification {
        return NotificationCompat.Builder(this, Constants.CHANNEL_SYNC_ID).apply {
            setSmallIcon(R.drawable.ic_default_book)
            setContentTitle(getString(R.string.notification_syncing_title))
            priority = NotificationCompat.PRIORITY_DEFAULT
            color = ContextCompat.getColor(applicationContext, android.R.color.holo_blue_dark)
            setContentText(getString(R.string.notification_syncing_message))
            setStyle(NotificationCompat.BigTextStyle().
            bigText(getString(R.string.notification_syncing_message)))
            setProgress(0, 0, true)
            setAutoCancel(false)
            setOngoing(true)
        }.build()
    }

    fun setStatus(st : LStatus) { _status.value = st }

    private fun finish(){
        _status.value = LStatus.STOPPED
        _status.removeObserver(selfObs)
        stopForeground(true)
        stopSelf()
    }
}

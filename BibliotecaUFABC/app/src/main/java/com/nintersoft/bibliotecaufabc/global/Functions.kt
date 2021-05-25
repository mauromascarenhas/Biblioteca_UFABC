package com.nintersoft.bibliotecaufabc.global

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.work.*
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.activities.BookViewerActivity
import com.nintersoft.bibliotecaufabc.activities.MainActivity
import com.nintersoft.bibliotecaufabc.model.AppContext
import com.nintersoft.bibliotecaufabc.model.AppDatabase
import com.nintersoft.bibliotecaufabc.model.renewal.BookRenewal
import com.nintersoft.bibliotecaufabc.model.search.BookSearch
import com.nintersoft.bibliotecaufabc.notifications.NotificationDisplay
import com.nintersoft.bibliotecaufabc.notifications.SyncNotificationDisplay
import com.nintersoft.bibliotecaufabc.synchronization.SyncWorker
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Methods which are useful in different parts of the application
 */
object Functions {
    /**
     * Configures the given #WebView with the application standard settings,
     * allowing it to run JavaScript content and keep cache
     * @param v : WebView
     */
    @SuppressWarnings("SetJavaScriptEnabled")
    fun configureWebView(v: WebView, wvc : WebViewClient){
        val settings = v.settings
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        v.webViewClient = wvc
    }

    /**
     * Retrieves the asset content as a text
     *
     * @param path     : Relative path to the desired asset (must be inside "assets" folder)
     * @return         : Returns the file content as a string or null if an exception has occurred
     */
    fun scriptFromAssets(path: String) : String{
        val sb = StringBuilder()
        val ins : InputStream = AppContext.context!!.assets.open(path)
        val br = BufferedReader(InputStreamReader(ins, StandardCharsets.UTF_8))

        var str: String?
        while (br.readLine().also { str = it } != null) sb.append(str).append("\n")
        br.close()
        return sb.toString()
    }

    /**
     * Fires BookViewerActivity execution. This method should be used only
     *  when the Card's onClick event is triggered
     *
     * @param book : Book details which its details
     * @param context : Context used to launch activity
     */
    fun viewBookDetails(book : BookSearch, context: Context){
        with (context){
            Intent(this, BookViewerActivity::class.java).also {
                it.putExtra(Constants.BOOK_CODE, book.code)
                startActivity(it)
            }
        }
    }

    /**
     * Opens a chooser dialog which allows the user to share some details
     *  about the book/work with whoever he wants, using any supported app.
     *  This method should be used only when the Cards onLongPress event is
     *  triggered
     *
     *  @param book : Book's basic details which is going to be shared
     *  @param context : Context used to create the activity chooser
     */
    fun shareBookDetails(book : BookSearch, context: Context){
        with (context){
            val bookShare = getString(R.string.share_book_structure, book.title,
                book.author, "${Constants.URL_LIBRARY_DETAILS}?codigo=${book.code}" +
                    Constants.MANDATORY_APPEND_URL_LIBRARY_DETAILS)

            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, bookShare)
            }.also {
                if (packageManager.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null)
                    startActivity(Intent.createChooser(it, getString(R.string.intent_share_book)))
            }
        }
    }

    /**
     * Creates notification channel according to the given params
     *
     * @param name : Channel name
     * @param description : Channel description
     * @param channelId : Channel ID
     */
    fun createNotificationChannel(name : CharSequence, description : String, channelId : String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            with (AppContext.context!!){
                getSystemService(NotificationManager::class.java)?.also {
                    it.createNotificationChannel(NotificationChannel(channelId, name,
                        NotificationManager.IMPORTANCE_HIGH).also {channel ->
                            channel.description = description
                        })
                }
            }
        }
    }

    /**
     * Creates a generic notification with a pending intent to RenewalActivity.
     * @param title_rId   : Id of the title string resource
     * @param message_rId : Id of the message string resource
     * @return            : Returns the notification already built
     */
    private fun createSyncNotification(title_rId : Int, message_rId : Int) : Notification{
        val context = AppContext.context!!
        return NotificationCompat.Builder(context, Constants.CHANNEL_SYNC_ID).apply {
            setSmallIcon(R.drawable.ic_default_book)
            setContentTitle(context.getString(title_rId))
            priority = NotificationCompat.PRIORITY_HIGH
            setVibrate(longArrayOf(750L, 750L))
            color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setContentText(context.getString(message_rId))
            setStyle(NotificationCompat.BigTextStyle().also {
                it.bigText(context.getString(message_rId))
            })
            setAutoCancel(false)
            setContentIntent(PendingIntent.getActivity(context,
                Constants.ACTIVITY_RENEWAL_REQUEST_CODE,
                Intent(context, MainActivity::class.java), 0))
        }.build()
    }

    /**
     * Creates a renewal notification with the given message.
     * If no message is passed to the method, it will build a generic one to be displayed
     * @param msg : Message which will be displayed in the notification body
     * @return        : Returns the notification already built
     */
    fun createRenewalNotification(msg : String?) : Notification{
        val c = AppContext.context!!
        return NotificationCompat.Builder(c, Constants.CHANNEL_RENEWAL_ID).apply {
            setSmallIcon(R.drawable.ic_default_book)
            setContentTitle(c.getString(R.string.notification_book_renewal_title))
            priority = NotificationCompat.PRIORITY_HIGH
            setVibrate(longArrayOf(750, 750))
            color = ContextCompat.getColor(c, R.color.colorPrimary)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setAutoCancel(true)
            setContentText(msg ?: c.getString(R.string.notification_book_renewal_content))
            setStyle(NotificationCompat.BigTextStyle().
                bigText(msg ?: c.getString(R.string.notification_book_renewal_content)))
            setContentIntent(PendingIntent.getActivity(c, Constants.ACTIVITY_RENEWAL_REQUEST_CODE,
                Intent(c, MainActivity::class.java), 0))
        }.build()
    }


    /**
     * Creates a notification with a pending intent to RenewalActivity and post it.
     * @param title_rId   : Id of the title string resource
     * @param message_rId : Id of the message string resource
     * @param id      : Notification id
     */
    fun createSyncNotification(title_rId : Int, message_rId : Int, id : Int){
        (AppContext.context?.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager)?.apply {
            notify(id, createSyncNotification(title_rId, message_rId))
        }
    }

    /**
     * Cancels the recurrent synchronization scheduled with
     * #schedulePeriodicSync(Context, long, long)
     *
     */
    fun cancelPeriodicSync(){
        WorkManager.getInstance(AppContext.context!!).
            cancelUniqueWork(Constants.WORK_SYNC_SERVICE_WORKER)
    }

    /**
     * Schedules a recurrent synchronization reminder within the given initial
     * delay and periodic interval (which must be in milliseconds)
     * Take notice that this method also removes previously scheduled requests
     *
     * @param periodic : Periodic interval in which the task must be executed (milliseconds)
     * @param flex     : Time in milliseconds in which the task may be executed after the timeout
     * @param delay    : Time in milliseconds to trigger the first synchronization operation
     */
    fun schedulePeriodicSyncReminder(periodic : Long, flex : Long, delay : Long = TimeUnit.DAYS.
                                         toMillis(Constants.SYNC_REMINDER_NOTIFICATION_INTERVAL)){
        val flexN = if (flex < PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS)
                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS else flex
        val periodicN = if (periodic < PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS)
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS else periodic

        WorkManager.getInstance(AppContext.context!!).enqueueUniquePeriodicWork(
            Constants.WORK_SYNC_NOTIFICATION,
            ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequest.Builder(SyncNotificationDisplay::class.java,
                periodicN, TimeUnit.MILLISECONDS, flexN, TimeUnit.MILLISECONDS).
                setInitialDelay(delay, TimeUnit.MILLISECONDS).build()
        )
    }

    /**
     * Schedules a recurrent synchronization procedure within the given initial
     * delay and periodic interval (which must be in milliseconds)
     * Take notice that this method also removes previously scheduled requests
     *
     * @param periodic : Periodic interval in which the task must be executed (milliseconds)
     * @param flex     : Time in milliseconds in which the task may be executed after
     *                              the timeout
     * @param delay    : Time in milliseconds to trigger the first synchronization operation
     */
    fun schedulePeriodicSync(periodic : Long, flex : Long, delay : Long = TimeUnit.MINUTES.
                                 toMillis(Constants.SYNC_EXECUTIONER_FIRST_DELAY_MIN)){
        val flexN = if (flex < PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS)
            PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS else flex
        val periodicN = if (periodic < PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS)
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS else periodic

        WorkManager.getInstance(AppContext.context!!).enqueueUniquePeriodicWork(
            Constants.WORK_SYNC_SERVICE_WORKER,
            ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequest.Builder(SyncWorker::class.java,
                periodicN, TimeUnit.MILLISECONDS, flexN, TimeUnit.MILLISECONDS).
                setInitialDelay(delay, TimeUnit.MILLISECONDS).
                setConstraints(Constants.SYNC_CONSTRAINTS).
                build()
        )
    }

    /**
     * Schedules notification alarms for every #BookRenewalProperties available at AppDatabase.DAO
     */
    fun scheduleRenewalAlarms(){
        val c = AppContext.context!!
        WorkManager.getInstance(c).cancelAllWorkByTag(Constants.WORK_RENEWAL_NOTIFICATION_TAG)
        if (!PreferenceManager.getDefaultSharedPreferences(c).
                getBoolean(c.getString(R.string.key_notification_enable_warning), true))
            return

        val aOffset = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(c).
            getString(c.getString(R.string.key_notification_warning_delay), "0")!!)
        val grpBooks = HashMap<Date, ArrayList<BookRenewal>>()
        AppDatabase.getInstance()?.bookRenewalDAO()?.getAll()?.value?.forEach {br ->
            grpBooks[br.date!!] = ArrayList<BookRenewal>().apply {
                if (br.date!! in grpBooks.keys) addAll(grpBooks[br.date!!]!!)
                add(br)
            }
        }

        grpBooks.keys.forEach {dt ->
            val carr = grpBooks[dt]
            if (carr.isNullOrEmpty()) return@forEach

            var cp = carr[0]
            for (el in carr) if (cp.id!! > el.id!!) cp = el

            val msg = if (carr.size == 1)
                c.getString(R.string.notification_book_renewal_specific_content, carr[0].title)
            else c.getString(R.string.notification_book_renewal_specific_content_multiple,
                StringBuilder().apply {
                    append("- ${carr[0].title};")
                    for (i in 1 until carr.size) append("\n- ${carr[i].title};")
                }.toString())

            for (k in 0 until 3){
                val calendar = Calendar.getInstance()
                calendar.time = cp.date!!
                calendar.add(Calendar.DAY_OF_MONTH, aOffset)

                val millis = calendar.time.time - Date().time
                if (millis > 0) scheduleBookNotification(millis,
                    ((cp.id!! + (500 * k)) % (Integer.MAX_VALUE.toLong() + 1)).toInt(), msg)
            }
        }
    }

    /**
     * Schedules a renewal notification/warning with the given Id and time (which must be in milliseconds)
     * Help from : https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
     *
     * @param initialDelay : Periodic interval in which the task must be executed (milliseconds)
     * @param notificationId : Unique identifier for the new notification (may be replaced if there is
     *                          another with the same ID)
     * @param message        : Message to be displayed
     */
    private fun scheduleBookNotification(initialDelay : Long, notificationId : Int,
                                         message : String?){
        val c = AppContext.context!!
        WorkManager.getInstance(c).enqueue(OneTimeWorkRequest.
            Builder(NotificationDisplay::class.java).apply {
                addTag(Constants.WORK_RENEWAL_NOTIFICATION_TAG)
                setInputData(Data.Builder().apply {
                    putInt(Constants.NOTIFICATION_ID, notificationId)
                    putString(Constants.NOTIFICATION_MESSAGE, message)
                }.build())
                setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            }.build())
    }

    /**
     *  Checks whether the given intent is callable
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun isCallable(i : Intent) : Boolean {
        return AppContext.context!!.packageManager.queryIntentActivities(i,
            PackageManager.MATCH_DEFAULT_ONLY).size > 0
    }
}
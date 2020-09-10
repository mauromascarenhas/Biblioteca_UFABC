package com.nintersoft.bibliotecaufabc.notifications

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions
import java.util.concurrent.TimeUnit

class SyncNotificationDisplay(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        PreferenceManager.getDefaultSharedPreferences(context).also { prefs ->
            val nextAlarm = System.currentTimeMillis() - prefs.getLong(context.
                getString(R.string.key_synchronization_schedule), 0)
            if (nextAlarm >= TimeUnit.DAYS.toMillis(Constants.SYNC_REMINDER_NOTIFICATION_INTERVAL)){
                Functions.createSyncNotification(
                    R.string.notification_sync_title,
                    R.string.notification_sync_message, Constants.SYNC_NOTIFICATION_REMINDER_ID)
            }
        }
        return Result.success()
    }

}
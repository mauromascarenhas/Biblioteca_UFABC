package com.nintersoft.bibliotecaufabc.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nintersoft.bibliotecaufabc.global.Constants
import com.nintersoft.bibliotecaufabc.global.Functions

class NotificationDisplay(private val context: Context, private val workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    override fun doWork(): Result {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.run {
            cancelAll()
            notify(workerParams.inputData.getInt(Constants.NOTIFICATION_ID, -1),
                Functions.createRenewalNotification(workerParams.inputData.
                    getString(Constants.NOTIFICATION_MESSAGE)))
        }

        return Result.success()
    }
}
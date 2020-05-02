package com.nintersoft.bibliotecaufabc.synchronization

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class SyncWorker(private val context : Context, params : WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        ContextCompat.startForegroundService(context, Intent(context, SyncService::class.java))
        return Result.success()
    }

}
package com.nintersoft.bibliotecaufabc.synchronization

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nintersoft.bibliotecaufabc.R
import com.nintersoft.bibliotecaufabc.global.Functions
import kotlinx.coroutines.*

class SyncWorker(private val context : Context, params : WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        Functions.logMsg("SYNC_WORKER_D_M", "starting...")
        val start = getLastSync()
        Functions.logMsg("SYNC_WORKER_D_M", "Last sync : $start")
        ContextCompat.startForegroundService(context, Intent(context, SyncService::class.java))
        runBlocking { delay(180000) }
        Functions.logMsg("SYNC_WORKER_D_M", "Current status ${SyncService.status}")
        return if (start == getLastSync()) Result.retry() else Result.success()
    }

    private fun getLastSync() : Long{
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(context.getString(R.string.key_synchronization_schedule), -1L)
    }
}
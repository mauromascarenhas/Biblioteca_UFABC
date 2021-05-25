package com.nintersoft.bibliotecaufabc.synchronization

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.nintersoft.bibliotecaufabc.R
import kotlinx.coroutines.*

class SyncWorker(private val context : Context, params : WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        val start = getLastSync()
        ContextCompat.startForegroundService(context, Intent(context, SyncService::class.java))
        runBlocking { delay(180000) }
        return if (start == getLastSync()) Result.retry() else Result.success()
    }

    private fun getLastSync() : Long{
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(context.getString(R.string.key_synchronization_schedule), -1L)
    }
}
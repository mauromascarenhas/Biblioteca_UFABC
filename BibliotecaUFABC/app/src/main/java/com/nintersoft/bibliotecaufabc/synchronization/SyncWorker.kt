package com.nintersoft.bibliotecaufabc.synchronization

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.nintersoft.bibliotecaufabc.synchronization.SyncService.Companion.LStatus
import java.util.*

class SyncWorker(private val context : Context, params : WorkerParameters)
    : ListenableWorker(context, params) {

    private var selfObs : Observer<LStatus>? = null

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { result ->
            if (SyncService.status.value == LStatus.STOPPED){
                (Handler(Looper.getMainLooper())).post {
                    selfObs = Observer { status ->
                        when (status) {
                            LStatus.FINISHED_FAILURE -> {
                                selfObs?.also { self -> SyncService.status.removeObserver(self) }
                                result.set(Result.retry())
                            }
                            LStatus.FINISHED_SUCCESS -> {
                                selfObs?.also { self -> SyncService.status.removeObserver(self) }
                                result.set(Result.success())
                            }
                            else -> return@Observer
                        }
                    }
                    SyncService.status.observeForever(selfObs!!)
                    ContextCompat.startForegroundService(context, Intent(context, SyncService::class.java))
                }
            }
            else result.set(Result.retry())
        }
    }

    override fun onStopped() {
        selfObs?.also { self -> SyncService.status.removeObserver(self) }
        super.onStopped()
    }
}
package com.nintersoft.bibliotecaufabc.synchronization;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nintersoft.bibliotecaufabc.R;

public class SyncManager extends Worker {

    private Long lastSync;
    private Context mContext;

    public SyncManager(@NonNull Context context, WorkerParameters params){
        super(context, params);

        mContext = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("StatementWithEmptyBody")
    public Result doWork() {
        // Acquires last sync time
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        lastSync = pref.getLong(mContext.getString(R.string.key_synchronization_schedule), System.currentTimeMillis());
        // Starts SyncService
        ContextCompat.startForegroundService(mContext, new Intent(mContext, SyncService.class));
        // Waits for completion
        while (isServiceRunning());
        // Checks whether the sync was successful or not
        return isSyncComplete() ? Result.success() : Result.retry();
    }

    private boolean isServiceRunning(){
        ActivityManager aManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (aManager != null)
            for (ActivityManager.RunningServiceInfo service : aManager.getRunningServices(Integer.MAX_VALUE))
                if (SyncService.class.getName().equals(service.service.getClassName())) return true;
        return false;
    }

    private boolean isSyncComplete(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Long cLastSync = pref.getLong(mContext.getString(R.string.key_synchronization_schedule), System.currentTimeMillis());
        return cLastSync > lastSync;
    }
}

package com.nintersoft.bibliotecaufabc.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class SyncExecutioner extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ContextCompat.startForegroundService(context, new Intent(context, SyncService.class));
    }
}

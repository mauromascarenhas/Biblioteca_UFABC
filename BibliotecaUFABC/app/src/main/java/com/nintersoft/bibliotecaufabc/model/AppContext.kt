package com.nintersoft.bibliotecaufabc.model

import android.app.Application

class AppContext : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    companion object {
        var context: AppContext? = null
            private set
    }
}
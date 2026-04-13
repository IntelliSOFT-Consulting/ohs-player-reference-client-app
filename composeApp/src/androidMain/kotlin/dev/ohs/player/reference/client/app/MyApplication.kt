package dev.ohs.player.reference.client.app

import android.app.Application
import android.content.Context


class MyApplication : Application() {

    companion object {
        // applicationContext is safe — it lives as long as the app process
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
package com.example.web_server_poc.application

import android.app.Application
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Plant a Timber Debug Tree
        Timber.plant(Timber.DebugTree())
    }
}
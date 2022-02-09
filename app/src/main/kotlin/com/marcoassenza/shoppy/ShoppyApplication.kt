package com.marcoassenza.shoppy

import android.app.Application
import timber.log.Timber

class ShoppyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
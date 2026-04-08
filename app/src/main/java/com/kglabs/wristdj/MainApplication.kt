package com.kglabs.wristdj

import android.app.Application
import com.kglabs.wristdj.BuildConfig
import timber.log.Timber

class MainApplication : Application() {
    companion object {
        private lateinit var myInstance: MainApplication
        fun getInstance(): MainApplication = myInstance
    }

    override fun onCreate() {
        super.onCreate()
        myInstance = this
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

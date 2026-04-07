package com.kglabs.wristdj

import android.app.Application
import com.kglabs.wristdj.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MainApplication : Application() {
    companion object {
        private var myInstance: MainApplication? = null
        fun getInstance(): MainApplication? {
            return myInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        myInstance = this
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

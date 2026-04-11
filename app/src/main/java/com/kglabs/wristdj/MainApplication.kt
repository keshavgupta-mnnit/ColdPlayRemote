package com.kglabs.wristdj

import android.app.Application
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }

    /**
     * A Timber tree that reports errors/exceptions to Firebase Crashlytics.
     */
    private class CrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            val crashlytics = FirebaseCrashlytics.getInstance()
            
            // Log the message to Crashlytics (adds to the crash report)
            crashlytics.log(message)

            if (t != null) {
                // Record the non-fatal exception
                crashlytics.recordException(t)
            } else if (priority >= Log.ERROR) {
                // If it's an error without a throwable, create a synthetic exception
                crashlytics.recordException(Throwable(message))
            }
        }
    }
}

package com.keshav.coldplayremote

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

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
    }
}
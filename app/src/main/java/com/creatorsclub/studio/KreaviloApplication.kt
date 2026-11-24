package com.creatorsclub.studio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KreaviloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

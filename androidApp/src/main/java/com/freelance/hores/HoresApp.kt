package com.freelance.hores

import android.app.Application
import com.freelance.hores.di.initKoin
import org.koin.android.ext.koin.androidContext

class HoresApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@HoresApp)
        }
    }
}

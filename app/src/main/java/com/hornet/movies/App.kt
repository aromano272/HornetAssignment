package com.hornet.movies

import android.app.Application
import com.hornet.movies.di.appModule
import com.hornet.movies.di.dataModule
import com.hornet.movies.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                appModule,
                presentationModule,
                dataModule,
            )
        }
    }

}
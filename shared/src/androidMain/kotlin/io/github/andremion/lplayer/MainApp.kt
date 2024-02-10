package io.github.andremion.lplayer

import android.app.Application
import io.github.andremion.lplayer.di.initDI
import org.koin.android.ext.koin.androidContext

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initDI().androidContext(this)
    }
}

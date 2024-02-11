package io.github.andremion.musicplayer

import android.app.Application
import io.github.andremion.musicplayer.di.initDI
import org.koin.android.ext.koin.androidContext

class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initDI().androidContext(this)
    }
}

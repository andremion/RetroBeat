package io.github.andremion.musicplayer.di

import io.github.andremion.musicplayer.data.di.DataModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initDI(): KoinApplication =
    startKoin {
        modules(
            DataModule.module,
        )
    }

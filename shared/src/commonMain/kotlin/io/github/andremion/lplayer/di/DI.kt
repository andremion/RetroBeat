package io.github.andremion.lplayer.di

import io.github.andremion.lplayer.data.di.DataModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initDI(): KoinApplication =
    startKoin {
        modules(
            DataModule.module,
        )
    }

package io.github.andremion.musicplayer.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initDI(): KoinApplication =
    startKoin {
        modules(
        )
    }

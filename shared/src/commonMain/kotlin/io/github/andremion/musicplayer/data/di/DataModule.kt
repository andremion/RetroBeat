package io.github.andremion.musicplayer.data.di

import org.koin.dsl.module

object DataModule {
    val module = module {
        includes(InternalDataModule.module)
    }
}

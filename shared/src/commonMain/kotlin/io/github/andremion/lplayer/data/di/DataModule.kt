package io.github.andremion.lplayer.data.di

import org.koin.dsl.module

object DataModule {
    val module = module {
        includes(InternalDataModule.module)
    }
}

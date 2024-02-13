package io.github.andremion.musicplayer.data.di

import io.github.andremion.musicplayer.data.MusicRepositoryImpl
import io.github.andremion.musicplayer.domain.MusicRepository
import org.koin.dsl.module

object DataModule {
    val module = module {
        includes(InternalDataModule.module)
        single<MusicRepository> {
            MusicRepositoryImpl()
        }
    }
}

package io.github.andremion.musicplayer.data.di

import io.github.andremion.musicplayer.data.AudioPlayerImpl
import io.github.andremion.musicplayer.domain.AudioPlayer
import org.koin.dsl.module

internal actual object InternalDataModule {
    actual val module = module {
        factory<AudioPlayer> {
            AudioPlayerImpl(
                context = get()
            )
        }
    }
}

package io.github.andremion.lplayer.data.di

import io.github.andremion.lplayer.data.AudioPlayerImpl
import io.github.andremion.lplayer.domain.AudioPlayer
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

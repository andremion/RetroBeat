package io.github.andremion.musicplayer.presentation.di

import io.github.andremion.musicplayer.presentation.player.PlayerViewModel
import org.koin.dsl.module

object PresentationModule {
    val module = module {
        factory {
            PlayerViewModel(
                repository = get(),
                audioPlayer = get()
            )
        }
    }
}

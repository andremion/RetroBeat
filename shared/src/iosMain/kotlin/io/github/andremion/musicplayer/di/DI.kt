package io.github.andremion.musicplayer.di

import io.github.andremion.musicplayer.domain.AudioPlayer
import org.koin.dsl.module

/**
 * Initializes the DI and provides dependencies from Swift.
 * These dependencies are need because they are not multiplatform libraries.
 */
fun initDI(audioPlayer: AudioPlayer) {
    initDI().modules(
        module {
            factory { audioPlayer }
        }
    )
}

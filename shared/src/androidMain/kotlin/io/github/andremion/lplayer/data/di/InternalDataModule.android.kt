package io.github.andremion.lplayer.data.di

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import io.github.andremion.lplayer.data.AudioPlayerImpl
import io.github.andremion.lplayer.domain.AudioPlayer
import org.koin.dsl.module

internal actual object InternalDataModule {
    actual val module = module {
        factory {
            ExoPlayer.Builder(/* context = */ get())
                // Configuration for audio focus.
                .setAudioAttributes(
                    /* audioAttributes = */ AudioAttributes.Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(),
                    /* handleAudioFocus = */ true
                )
                // Sets whether the player should pause automatically
                // when audio is rerouted from a headset to device speakers
                .setHandleAudioBecomingNoisy(true)
                .build()
        }
        factory<AudioPlayer> {
            AudioPlayerImpl(
                player = get()
            )
        }
    }
}

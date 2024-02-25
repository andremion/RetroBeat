/*
 *    Copyright 2024. André Luiz Oliveira Rêgo
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.andremion.musicplayer.component.player.di

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import io.github.andremion.musicplayer.component.player.AudioPlayer
import io.github.andremion.musicplayer.component.player.AudioPlayerImpl
import org.koin.dsl.module

actual object PlayerModule {
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
                context = get()
            )
        }
    }
}

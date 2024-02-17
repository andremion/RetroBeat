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

package io.github.andremion.musicplayer.component.player

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface AudioPlayer {

    val state: StateFlow<State>
    val currentTrack: StateFlow<Track?>

    fun initialize(onInitialized: () -> Unit)
    fun setTracks(tracks: List<Track>)
    fun play()
    fun updateProgress()
    fun pause()
    fun skipToPrevious()
    fun skipToNext()
    fun seekBackward()
    fun seekForward()
    fun toggleRepeatMode()
    fun toggleShuffleMode()
    fun releasePlayer() // There is a `release` function in ObjectiveC.NSObject already. So I had to use a Player suffix.

    data class State(
        val isPlaying: Boolean = false,
        val position: Float = -0f,
        val time: Duration = 0.milliseconds,
        val duration: Duration = 0.milliseconds,
        val repeatMode: RepeatMode = RepeatMode.Off,
        val isShuffleModeOn: Boolean = false,
    )

    data class Track(
        val id: String,
        val uri: String,
        val metadata: Metadata,
    ) {
        data class Metadata(
            val title: String,
            val artist: String,
            val albumTitle: String,
            val artworkUri: String,
        )
    }

    enum class RepeatMode { Off, One, All }
}

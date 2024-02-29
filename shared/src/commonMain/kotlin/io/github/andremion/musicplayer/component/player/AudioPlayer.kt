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
import kotlin.time.Duration.Companion.seconds

internal const val DEFAULT_SEEK_BACK_INCREMENT = 5
internal const val DEFAULT_SEEK_FORWARD_INCREMENT = 15

interface AudioPlayer {

    val seekBackIncrementInSeconds: Int
    val seekForwardIncrementInSeconds: Int

    val currentTrack: StateFlow<Track?>
    val playback: StateFlow<Playback>

    fun initialize(onInitialized: () -> Unit)
    fun setTracks(tracks: List<Track>)
    fun playPause()
    fun play(trackIndex: Int)
    fun updateProgress()
    fun skipToPrevious()
    fun skipToNext()
    fun seekBackward()
    fun seekForward()
    fun toggleRepeatMode()
    fun toggleShuffleMode()
    fun releasePlayer() // There is a `release` function in ObjectiveC.NSObject already. So I had to use the `Player` suffix.

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

    data class Playback(
        val isLoading: Boolean = false,
        val isPlaying: Boolean = false,
        val hasError: Boolean = false,
        val progress: Progress = Progress(),
        val duration: Duration = (-0).seconds,
        val repeatMode: RepeatMode = RepeatMode.Off,
        val isShuffleModeOn: Boolean = false,
    ) {

        data class Progress(
            val position: Float = -0f,
            val time: Duration = (-0).seconds,
            // It can be used to emit data even if the data is the same.
            // This is useful for the update progress function to work properly.
            val timestamp: Long = 0,
        )
    }

    enum class RepeatMode {
        Off, One, All;

        fun toggle(): RepeatMode =
        // Produce a circular iteration over the enum values
            // to create a map where every key is associated with the next value.
            (entries + entries.first())
                .zipWithNext { a, b -> a to b }
                .toMap()
                .getValue(this)
    }
}

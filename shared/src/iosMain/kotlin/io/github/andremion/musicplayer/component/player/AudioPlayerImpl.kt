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

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.addBoundaryTimeObserverForTimes
import platform.AVFoundation.asset
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.AVFoundation.valueWithCMTime
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL.Companion.URLWithString
import platform.Foundation.NSValue
import platform.darwin.dispatch_get_main_queue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalForeignApi::class)
internal class AudioPlayerImpl : AudioPlayer {

    private var player = AVPlayer()
    private var currentItemIndex = 0
    private var tracks = emptyList<AudioPlayer.Track>()
    private var timeObserverToken: Any? = null

    override val seekBackIncrementInSeconds: Int = DEFAULT_SEEK_BACK_INCREMENT
    override val seekForwardIncrementInSeconds: Int = DEFAULT_SEEK_FORWARD_INCREMENT

    private val mutableState = MutableStateFlow(AudioPlayer.State())
    override val state: StateFlow<AudioPlayer.State> = mutableState.asStateFlow()

    private val mutableTrack = MutableStateFlow<AudioPlayer.Track?>(null)
    override val currentTrack: StateFlow<AudioPlayer.Track?> = mutableTrack.asStateFlow()

    override fun initialize(onInitialized: () -> Unit) {
        // iOS doesn't need to asynchronously initialize anything so far
        onInitialized()
    }

    override fun setTracks(tracks: List<AudioPlayer.Track>) {
        require(tracks.isNotEmpty()) { "Tracks list must not be empty" }
        this.tracks = tracks
        setCurrentItem(index = 0)
    }

    override fun playPause() {
        if (player.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
            pause()
        } else {
            play()
        }
    }

    override fun play(trackIndex: Int) {
        setCurrentItem(index = trackIndex) {
            play()
        }
    }

    override fun updateProgress() {
        player.currentItem?.let { currentItem ->
            mutableState.update { state ->
                val currentTime = CMTimeGetSeconds(player.currentTime()).seconds
                val duration = CMTimeGetSeconds(currentItem.asset.duration).seconds
                state.copy(
                    position = (currentTime / state.duration).toFloat(),
                    time = currentTime,
                    duration = duration,
                    // Make sure the state is gonna be emitted even if the state is the same.
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                )
            }
        }
    }

    override fun skipToPrevious() {
        val previousIndex = if (currentItemIndex > 0) {
            currentItemIndex - 1
        } else {
            if (state.value.repeatMode == AudioPlayer.RepeatMode.All) {
                tracks.size - 1
            } else {
                -1
            }
        }
        if (previousIndex == -1) {
            player.seekToTime(
                CMTimeMakeWithSeconds(
                    seconds = 0.0,
                    preferredTimescale = 1
                )
            )
        } else {
            play(previousIndex)
        }
    }

    override fun skipToNext() {
        val nextIndex = if (currentItemIndex < tracks.size - 1) {
            currentItemIndex + 1
        } else {
            when (state.value.repeatMode) {
                AudioPlayer.RepeatMode.One -> currentItemIndex
                AudioPlayer.RepeatMode.All -> 0
                AudioPlayer.RepeatMode.Off -> -1
            }
        }
        if (nextIndex == -1) {
            stop()
        } else {
            play(nextIndex)
        }
    }

    override fun seekBackward() {
        val time = CMTimeMakeWithSeconds(
            seconds = CMTimeGetSeconds(player.currentTime()) - seekBackIncrementInSeconds,
            preferredTimescale = 1
        )
        player.seekToTime(time)
    }

    override fun seekForward() {
        val time = CMTimeMakeWithSeconds(
            seconds = CMTimeGetSeconds(player.currentTime()) + seekForwardIncrementInSeconds,
            preferredTimescale = 1
        )
        player.seekToTime(time)
    }

    override fun toggleRepeatMode() {
        mutableState.update { state ->
            state.copy(repeatMode = state.repeatMode.toggle())
        }
    }

    override fun toggleShuffleMode() {
        TODO("Not yet implemented")
    }

    override fun releasePlayer() {
        player.pause()
    }

    private fun play() {
        player.play()
        mutableState.update { state ->
            state.copy(isPlaying = true)
        }
        updateProgress()
    }

    private fun pause() {
        player.pause()
        mutableState.update { state ->
            state.copy(isPlaying = false)
        }
        updateProgress()
    }

    private fun stop() {
        player.pause()
        setCurrentItem(currentItemIndex) {
            mutableState.update { state ->
                state.copy(isPlaying = false)
            }
        }
    }

    private fun setCurrentItem(index: Int, onLoaded: () -> Unit = {}) {
        currentItemIndex = index

        // Remove any previous time observer
        timeObserverToken?.let { timeObserverToken ->
            player.removeTimeObserver(timeObserverToken)
            this.timeObserverToken = null
        }

        val currentTrack = tracks[index]
        mutableTrack.update { currentTrack }

        val url = URLWithString(currentTrack.uri)!!
        AVPlayerItem(url).apply {
            player.replaceCurrentItemWithPlayerItem(this)
            asset.loadValuesAsynchronouslyForKeys(listOf("duration")) {
                // Skip to the next track when the current one ends
                scheduleNextSkipOnEndPlaying(duration = asset.duration)
                updateProgress()
                onLoaded()
            }
        }
    }

    private fun scheduleNextSkipOnEndPlaying(duration: CValue<CMTime>) {
        val time = CMTimeMakeWithSeconds(seconds = CMTimeGetSeconds(duration), preferredTimescale = 1)
        timeObserverToken = player.addBoundaryTimeObserverForTimes(
            times = listOf(NSValue.valueWithCMTime(time)),
            queue = dispatch_get_main_queue()
        ) {
            skipToNext()
        }
    }
}

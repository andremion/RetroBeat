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

import io.github.aakira.napier.Napier
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFoundation.AVKeyValueStatusLoaded
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
import platform.AVFoundation.timeControlStatus
import platform.AVFoundation.valueWithCMTime
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMultiply
import platform.Foundation.NSURL.Companion.URLWithString
import platform.Foundation.NSValue
import platform.darwin.dispatch_get_main_queue
import kotlin.time.Duration.Companion.seconds

private const val LogTag = "AudioPlayer"

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
        setCurrentItem(index = trackIndex)
        play()
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun updateProgress() {
        mutableState.update { state ->
            val currentTime = CMTimeGetSeconds(player.currentTime()).seconds
            state.copy(
                position = (currentTime / state.duration).toFloat(),
                time = currentTime,
            )
        }
    }

    override fun skipToPrevious() {
        if (currentItemIndex > 0) {
            play(trackIndex = currentItemIndex - 1)
        }
    }

    override fun skipToNext() {
        if (currentItemIndex < tracks.size - 1) {
            play(trackIndex = currentItemIndex + 1)
        }
    }

    override fun seekBackward() {
        TODO("Not yet implemented")
    }

    override fun seekForward() {
        TODO("Not yet implemented")
    }

    override fun toggleRepeatMode() {
        TODO("Not yet implemented")
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

    private fun setCurrentItem(index: Int) {
        currentItemIndex = index

        val currentTrack = tracks[index]
        mutableTrack.update { currentTrack }

        val url = URLWithString(currentTrack.uri)!!
        val playerItem = AVPlayerItem(url)

        // Remove any previous time observer
        timeObserverToken?.let { timeObserverToken ->
            player.removeTimeObserver(timeObserverToken)
            this.timeObserverToken = null
        }

        player.pause()
        player.replaceCurrentItemWithPlayerItem(playerItem)

        updateDuration()
        updateProgress()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun updateDuration() {
        player.currentItem?.let { currentItem ->
            currentItem.asset.loadValuesAsynchronouslyForKeys(listOf("duration")) {
                if (currentItem.asset.statusOfValueForKey("duration", null) != AVKeyValueStatusLoaded) {
                    Napier.w("Error on loading duration", tag = LogTag)
                } else {
                    // Skip to next track when the current one ends
                    scheduleSkipToNextOnEndPlaying(duration = currentItem.asset.duration)
                    mutableState.update { state ->
                        state.copy(
                            duration = CMTimeGetSeconds(currentItem.asset.duration).seconds,
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun scheduleSkipToNextOnEndPlaying(duration: CValue<CMTime>) {
        val interval = CMTimeMultiply(time = duration, multiplier = 1)
        timeObserverToken = player.addBoundaryTimeObserverForTimes(
            times = listOf(NSValue.valueWithCMTime(interval)),
            queue = dispatch_get_main_queue()
        ) {
            skipToNext()
        }
    }
}

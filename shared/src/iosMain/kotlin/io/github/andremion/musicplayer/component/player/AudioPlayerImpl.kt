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

@file:OptIn(ExperimentalForeignApi::class)

package io.github.andremion.musicplayer.component.player

import io.github.aakira.napier.Napier
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerStatusFailed
import platform.AVFoundation.AVPlayerStatusUnknown
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
import platform.Foundation.NSKeyValueObservingOptionInitial
import platform.Foundation.NSKeyValueObservingOptionNew
import platform.Foundation.NSURL.Companion.URLWithString
import platform.Foundation.NSValue
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.foundation.NSKeyValueObservingProtocol
import kotlin.time.Duration.Companion.seconds

internal class AudioPlayerImpl : AudioPlayer {

    private lateinit var player: AVPlayer
    private lateinit var audioSession: AVAudioSession
    private var currentItemIndex = 0
    private var tracks = emptyList<AudioPlayer.Track>()
    private var timeObserverToken: Any? = null
    private var shuffleIndices = emptyList<Int>()

    override val seekBackIncrementInSeconds: Int = DEFAULT_SEEK_BACK_INCREMENT
    override val seekForwardIncrementInSeconds: Int = DEFAULT_SEEK_FORWARD_INCREMENT

    private val mutableTrack = MutableStateFlow<AudioPlayer.Track?>(null)
    override val currentTrack: StateFlow<AudioPlayer.Track?> = mutableTrack.asStateFlow()

    private val mutablePlayback = MutableStateFlow(AudioPlayer.Playback())
    override val playback: StateFlow<AudioPlayer.Playback> = mutablePlayback.asStateFlow()

    override fun initialize(onInitialized: () -> Unit) {
        if (!::player.isInitialized) {
            player = AVPlayer().apply {
                addObserver(
                    observer = playingObserver,
                    forKeyPath = "timeControlStatus",
                    options = NSKeyValueObservingOptionInitial and NSKeyValueObservingOptionNew,
                    context = null
                )
            }
            audioSession = AVAudioSession.sharedInstance()
            try {
                audioSession.setCategory(
                    category = AVAudioSessionCategoryPlayback,
                    error = null
                )
            } catch (e: Exception) {
                Napier.w("Failed to set the audio session configuration", tag = AudioPlayer.LogTag)
            }
            onInitialized()
        }
    }

    override fun setTracks(tracks: List<AudioPlayer.Track>) {
        require(tracks.isNotEmpty()) { "Tracks list must not be empty" }
        this.tracks = tracks
        setCurrentItem(index = 0)
    }

    override fun playPause() {
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    override fun play(trackIndex: Int) {
        setCurrentItem(index = trackIndex)
        play()
    }

    override fun updateProgress() {
        player.currentItem?.let { currentItem ->
            mutablePlayback.update { playback ->
                val currentTime = CMTimeGetSeconds(player.currentTime()).seconds
                val duration = CMTimeGetSeconds(currentItem.asset.duration).seconds
                playback.copy(
                    progress = AudioPlayer.Playback.Progress(
                        position = (currentTime / playback.duration).toFloat(),
                        time = currentTime,
                        // Make sure the state is gonna be emitted even if the state is the same.
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                    ),
                    duration = duration,
                )
            }
        }
    }

    override fun skipToPrevious() {
        // Move to the previous track if the current one is not the first one
        val previousIndex = if (currentItemIndex > 0) {
            currentItemIndex - 1
        } else {
            // Move to the last track if the current one is the first one
            // and the repeat mode is set to all
            if (playback.value.repeatMode == AudioPlayer.RepeatMode.All) {
                tracks.size - 1
            } else {
                // Otherwise, it will seek to the initial time
                -1
            }
        }
        if (previousIndex == -1) {
            seekToInitialTime()
        } else {
            play(previousIndex)
        }
    }

    override fun skipToNext() {
        // Do not move to the next track if the repeat mode is set to one
        val nextIndex = if (playback.value.repeatMode == AudioPlayer.RepeatMode.One) {
            currentItemIndex
        } else {
            // Move to the next track if the current one is not the last one
            if (currentItemIndex < tracks.size - 1) {
                currentItemIndex + 1
            } else {
                // Move to the first track if the repeat mode is set to all
                if (playback.value.repeatMode == AudioPlayer.RepeatMode.All) {
                    0
                } else {
                    // Otherwise, it will stop
                    -1
                }
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
        mutablePlayback.update { playback ->
            playback.copy(repeatMode = playback.repeatMode.toggle())
        }
    }

    override fun toggleShuffleMode() {
        mutablePlayback.update { playback ->
            val isShuffleModeOn = !playback.isShuffleModeOn
            if (isShuffleModeOn) {
                shuffleIndices = tracks.indices.shuffled()
            }
            playback.copy(isShuffleModeOn = isShuffleModeOn)
        }
    }

    override fun releasePlayer() {
        stop()
        player.removeObserver(playingObserver, "timeControlStatus")
    }

    private fun activateAudioSession() {
        updateAudioSessionActive(active = true)
    }

    private fun deactivateAudioSession() {
        updateAudioSessionActive(active = false)
    }

    private fun updateAudioSessionActive(active: Boolean) {
        try {
            audioSession.setActive(
                active = active,
                withOptions = AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation,
                error = null
            )
        } catch (e: Exception) {
            Napier.w("Failed to update the audio session active to $active", tag = AudioPlayer.LogTag)
        }
    }

    private fun play() {
        activateAudioSession()
        player.play()
        updateProgress()
    }

    private fun pause() {
        player.pause()
        updateProgress()
        deactivateAudioSession()
    }

    private fun stop() {
        player.pause()
        seekToInitialTime()
        updateProgress()
        deactivateAudioSession()
    }

    private fun seekToInitialTime() {
        player.seekToTime(
            CMTimeMakeWithSeconds(
                seconds = 0.0,
                preferredTimescale = 1
            )
        )
    }

    private fun setCurrentItem(index: Int) {
        currentItemIndex = index

        removeCurrentItemObservers()

        val currentTrack = if (playback.value.isShuffleModeOn) {
            tracks[shuffleIndices[index]]
        } else {
            tracks[index]
        }
        mutableTrack.update { currentTrack }

        val url = URLWithString(currentTrack.uri)!!
        AVPlayerItem(url).apply {
            player.replaceCurrentItemWithPlayerItem(this)
            addObserver(
                observer = statusObserver,
                forKeyPath = "status",
                options = NSKeyValueObservingOptionInitial and NSKeyValueObservingOptionNew,
                context = null
            )
            asset.loadValuesAsynchronouslyForKeys(listOf("duration")) {
                // Skip to the next track when the current one ends
                scheduleNextSkipOnEndPlaying(duration = asset.duration)
                updateProgress()
            }
        }
    }

    private fun removeCurrentItemObservers() {
        timeObserverToken?.let { timeObserverToken ->
            player.removeTimeObserver(timeObserverToken)
            this.timeObserverToken = null
        }
        player.currentItem?.removeObserver(statusObserver, "status")
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

    private val statusObserver: NSObject = object : NSObject(), NSKeyValueObservingProtocol {

        override fun observeValueForKeyPath(
            keyPath: String?,
            ofObject: Any?,
            change: Map<Any?, *>?,
            context: COpaquePointer?
        ) {
            mutablePlayback.update { playback ->
                playback.copy(
                    isLoading = player.currentItem?.status == AVPlayerStatusUnknown,
                    hasError = player.currentItem?.status == AVPlayerStatusFailed
                )
            }
        }
    }

    private val playingObserver: NSObject = object : NSObject(), NSKeyValueObservingProtocol {

        override fun observeValueForKeyPath(
            keyPath: String?,
            ofObject: Any?,
            change: Map<Any?, *>?,
            context: COpaquePointer?
        ) {
            mutablePlayback.update { playback ->
                playback.copy(
                    isPlaying = player.isPlaying
                )
            }
        }
    }
}

private val AVPlayer.isPlaying: Boolean
    get() = timeControlStatus == AVPlayerTimeControlStatusPlaying

package io.github.andremion.musicplayer.data.player

import io.github.andremion.musicplayer.domain.AudioPlayer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Abstract implementation of [AudioPlayer] to encapsulate [StateFlow] and [SharedFlow],
 * so Swift won't complain about these types.
 */
abstract class AbstractAudioPlayer : AudioPlayer {

    private val mutableState = MutableStateFlow(AudioPlayer.State())
    override val state: StateFlow<AudioPlayer.State> = mutableState.asStateFlow()

    private val mutableTrack = MutableStateFlow<AudioPlayer.Track?>(null)
    override val currentTrack: StateFlow<AudioPlayer.Track?> = mutableTrack.asStateFlow()

    private val mutableEvents = MutableSharedFlow<AudioPlayer.Event>(extraBufferCapacity = 1)
    override val events: SharedFlow<AudioPlayer.Event> = mutableEvents.asSharedFlow()

    fun emitState(function: (AudioPlayer.State) -> AudioPlayer.State) {
        mutableState.update(function)
    }

    fun emitTrack(function: (AudioPlayer.Track?) -> AudioPlayer.Track?) {
        mutableTrack.update(function)
    }

    fun emitEvent(event: AudioPlayer.Event) {
        mutableEvents.tryEmit(event)
    }
}

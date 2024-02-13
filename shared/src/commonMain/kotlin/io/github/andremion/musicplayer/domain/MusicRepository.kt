package io.github.andremion.musicplayer.domain

import io.github.andremion.musicplayer.domain.entity.Playlist
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getPlaylist(): Flow<Playlist>
}

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

package io.github.andremion.musicplayer.data.api.deezer.mapper

import io.github.andremion.musicplayer.data.api.deezer.response.GetPlaylistByIdResponse
import io.github.andremion.musicplayer.data.api.deezer.response.SearchPlaylistResponse
import io.github.andremion.musicplayer.domain.entity.Music
import io.github.andremion.musicplayer.domain.entity.Playlist
import kotlin.time.Duration.Companion.seconds

internal fun SearchPlaylistResponse.toEntity(): List<Playlist> =
    data.map { playlist ->
        Playlist(
            id = playlist.id.toString(),
            title = playlist.title,
            description = "",
            musics = emptyList(),
        )
    }

internal fun GetPlaylistByIdResponse.toEntity(): Playlist =
    Playlist(
        id = id.toString(),
        title = title,
        description = description,
        musics = tracks.data.map { track ->
            Music(
                id = track.id.toString(),
                uri = track.preview,
                title = track.title,
                duration = track.duration.seconds,
                artist = track.artist.name,
                album = Music.Album(
                    title = track.album.title,
                    art = track.album.cover_big
                )
            )
        }
    )

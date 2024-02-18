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

package io.github.andremion.musicplayer.data.api.deezer.response

import kotlinx.serialization.Serializable

@Serializable
internal data class GetPlaylistByIdResponse(
    val id: Long,
    val title: String,
    val description: String,
    val picture_big: String,
    val tracks: Tracks
) {

    @Serializable
    data class Tracks(
        val data: List<Track>
    ) {

        @Serializable
        data class Track(
            val id: Long,
            val title: String,
            val duration: Long,
            val preview: String,
            val artist: Artist,
            val album: Album
        ) {

            @Serializable
            data class Artist(
                val name: String
            )

            @Serializable
            data class Album(
                val title: String,
                val cover_big: String
            )
        }
    }
}

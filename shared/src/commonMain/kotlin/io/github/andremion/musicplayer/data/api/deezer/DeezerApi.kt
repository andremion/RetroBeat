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

package io.github.andremion.musicplayer.data.api.deezer

import io.github.andremion.musicplayer.data.api.deezer.response.GetPlaylistByIdResponse
import io.github.andremion.musicplayer.data.api.deezer.response.SearchPlaylistResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

internal class DeezerApi(private val httpClient: HttpClient) {

    suspend fun searchPlaylist(query: String): SearchPlaylistResponse =
        httpClient.get("search/playlist?q=$query").body()

    suspend fun getPlaylistById(playlistId: Long): GetPlaylistByIdResponse =
        httpClient.get("playlist/$playlistId").body()
}

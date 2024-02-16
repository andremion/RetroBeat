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

package io.github.andremion.musicplayer.data

import io.github.andremion.musicplayer.domain.MusicRepository
import io.github.andremion.musicplayer.domain.entity.Music
import io.github.andremion.musicplayer.domain.entity.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class MusicRepositoryImpl : MusicRepository {

    override fun getPlaylist(): Flow<Playlist> =
        flowOf(
            Playlist(
                id = "4884747864",
                title = "Summer Hits",
                description = "The perfect soundtrack to an endless sunny summer",
                musics = listOf(
                    Music(
                        id = "1644464022",
                        uri = "https://cdns-preview-d.dzcdn.net/stream/c-ddf3ecfe031b0e38be1f7cef597d6af1-7.mp3",
                        title = "Calm Down",
                        artist = "Rema",
                        album = Music.Album(
                            title = "Calm Down",
                            art = "https://e-cdns-images.dzcdn.net/images/cover/3071378af24d789b8fc69e95162041e4/500x500-000000-80-0-0.jpg"
                        ),
                    ),
                    Music(
                        id = "1881003417",
                        uri = "https://cdns-preview-8.dzcdn.net/stream/c-8c851ce0de485e773832810743ad168d-3.mp3",
                        title = "Rush",
                        artist = "Ayra Starr",
                        album = Music.Album(
                            title = "Rush",
                            art = "https://e-cdns-images.dzcdn.net/images/cover/a73bed954d61b52564118ac926925d76/500x500-000000-80-0-0.jpg"
                        ),
                    ),
                    Music(
                        id = "2210576197",
                        uri = "https://cdns-preview-6.dzcdn.net/stream/c-6ea579e54652712f1beeb306d56c5eb2-3.mp3",
                        title = "Cupid (Twin Ver.)",
                        artist = "Fifty Fifty",
                        album = Music.Album(
                            title = "The Beginning: Cupid",
                            art = "https://e-cdns-images.dzcdn.net/images/cover/d4fd8cda9ed75643f459f4c8406f0070/500x500-000000-80-0-0.jpg"
                        ),
                    ),
                    Music(
                        id = "2299840635",
                        uri = "https://cdns-preview-6.dzcdn.net/stream/c-6f0ea5e28ac1d2d13bcd04fccb3ee220-3.mp3",
                        title = "Dance The Night (From Barbie The Album)",
                        artist = "Dua Lipa",
                        album = Music.Album(
                            title = "Dance The Night (From Barbie The Album)",
                            art = "https://e-cdns-images.dzcdn.net/images/cover/67bbf9fc8e49fc8d373c91963061572b/500x500-000000-80-0-0.jpg"
                        ),
                    ),
                    Music(
                        id = "737967292",
                        uri = "https://cdns-preview-4.dzcdn.net/stream/c-48e488b448a805fff7ad99a43ed63f16-6.mp3",
                        title = "Cruel Summer",
                        artist = "Taylor Swift",
                        album = Music.Album(
                            title = "Lover",
                            art = "https://e-cdns-images.dzcdn.net/images/cover/6111c5ab9729c8eac47883e4e50e9cf8/500x500-000000-80-0-0.jpg"
                        ),
                    ),
                )
            )
        )
}

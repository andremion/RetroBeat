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
                tracks = listOf(
                    Music(
                        id = "1644464022",
                        uri = "https://cdns-preview-d.dzcdn.net/stream/c-ddf3ecfe031b0e38be1f7cef597d6af1-7.mp3",
                        title = "Calm Down",
                        artist = "Rema",
                        album = Music.Album(
                            title = "Calm Down",
                            art = "https://api.deezer.com/album/292908332/image"
                        )
                    ),
                    Music(
                        id = "1881003417",
                        uri = "https://cdns-preview-8.dzcdn.net/stream/c-8c851ce0de485e773832810743ad168d-3.mp3",
                        title = "Rush",
                        artist = "Ayra Starr",
                        album = Music.Album(
                            title = "Rush",
                            art = "https://api.deezer.com/album/349702977/image"
                        )
                    ),
                    Music(
                        id = "2210576197",
                        uri = "https://cdns-preview-6.dzcdn.net/stream/c-6ea579e54652712f1beeb306d56c5eb2-3.mp3",
                        title = "Cupid (Twin Ver.)",
                        artist = "Fifty Fifty",
                        album = Music.Album(
                            title = "The Beginning: Cupid",
                            art = "https://api.deezer.com/album/422366867/image"
                        )
                    ),
                    Music(
                        id = "2299840635",
                        uri = "https://cdns-preview-6.dzcdn.net/stream/c-6f0ea5e28ac1d2d13bcd04fccb3ee220-3.mp3",
                        title = "Dance The Night (From Barbie The Album)",
                        artist = "Dua Lipa",
                        album = Music.Album(
                            title = "Dance The Night (From Barbie The Album)",
                            art = "https://api.deezer.com/album/446073025/image"
                        )
                    ),
                    Music(
                        id = "737967292",
                        uri = "https://cdns-preview-4.dzcdn.net/stream/c-48e488b448a805fff7ad99a43ed63f16-6.mp3",
                        title = "Cruel Summer",
                        artist = "Taylor Swift",
                        album = Music.Album(
                            title = "Lover",
                            art = "https://api.deezer.com/album/108447472/image"
                        )
                    ),
                )
            )
        )
}

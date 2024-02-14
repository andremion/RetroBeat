package io.github.andremion.musicplayer.domain.entity

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    val musics: List<Music>
)

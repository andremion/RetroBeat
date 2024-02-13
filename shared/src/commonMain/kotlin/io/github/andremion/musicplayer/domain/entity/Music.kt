package io.github.andremion.musicplayer.domain.entity

data class Music(
    val id: String,
    val uri: String,
    val title: String,
    val artist: String,
    val album: Album,
) {
    data class Album(
        val title: String,
        val art: String
    )
}

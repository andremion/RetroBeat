package io.github.andremion.musicplayer

import androidx.compose.runtime.Composable
import io.github.andremion.musicplayer.ui.home.PlayerScreen
import moe.tlaster.precompose.PreComposeApp

@Composable
fun MusicPlayer() {
    PreComposeApp {
        PlayerScreen()
    }
}

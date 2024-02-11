package io.github.andremion.musicplayer

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import io.github.andremion.musicplayer.ui.home.PlayerScreen
import io.github.andremion.musicplayer.ui.theme.AppTheme
import moe.tlaster.precompose.PreComposeApp

@Composable
fun MusicPlayer() {
    PreComposeApp {
        AppTheme {
            Surface {
                PlayerScreen()
            }
        }
    }
}

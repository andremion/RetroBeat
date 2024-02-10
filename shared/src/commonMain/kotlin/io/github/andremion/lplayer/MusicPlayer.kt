package io.github.andremion.lplayer

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import io.github.andremion.lplayer.ui.home.PlayerScreen
import io.github.andremion.lplayer.ui.theme.AppTheme
import moe.tlaster.precompose.PreComposeApp

@Composable
fun LPlayer() {
    PreComposeApp {
        AppTheme {
            Surface {
                PlayerScreen()
            }
        }
    }
}

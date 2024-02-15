package io.github.andremion.musicplayer

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController =
    ComposeUIViewController { MusicPlayer() }

package io.github.andremion.lplayer.ui.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun rememberMovableContent(
    content: @Composable (modifier: Modifier) -> Unit
): @Composable (modifier: Modifier) -> Unit =
    remember { movableContentOf(content) }

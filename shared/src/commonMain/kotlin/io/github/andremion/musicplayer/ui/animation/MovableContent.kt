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

package io.github.andremion.musicplayer.ui.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Remember a movable Composable content.
 *
 * This is useful when we want to animate a Composable content and we don't want to re-compose it
 * when it's moved from one parent to another.
 *
 * There are some cases that we do want to re-compose the content due to the data changes.
 * In this case, we can use the [key1] and [key2] parameters to re-invalidate the remembered content.
 *
 * @param key1 A key to re-invalidate the remembered content.
 * @param key2 A second key to re-invalidate the remembered content.
 * @param content A movable Composable content that will be remembered.
 */
@Composable
inline fun rememberMovableContent(
    key1: Any? = null,
    key2: Any? = null,
    crossinline content: @Composable (modifier: Modifier) -> Unit
): @Composable (modifier: Modifier) -> Unit =
    remember(key1, key2) { movableContentOf { modifier -> content(modifier) } }

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.alexzhirkevich.compottie.LottieComposition
import io.github.alexzhirkevich.compottie.LottieCompositionSpec.JsonString
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi
import retrobeat.shared.generated.resources.Res
import kotlin.jvm.JvmInline

sealed interface LottieCompositionSpec {

    /**
     * Load an animation from resources.
     */
    @JvmInline
    value class AnimationRes(val name: String) : LottieCompositionSpec
}

@Composable
fun rememberLottieComposition(spec: LottieCompositionSpec): State<LottieComposition?> =
    when (val data = rememberLottieData(spec)) {
        null -> mutableStateOf(null)
        else -> rememberLottieComposition(data)
    }

@Composable
private fun rememberLottieData(spec: LottieCompositionSpec): JsonString? {
    var data by remember { mutableStateOf<JsonString?>(null) }
    LaunchedEffect(spec) {
        data = when (spec) {
            is LottieCompositionSpec.AnimationRes -> {
                loadAnimationData(spec)
            }
        }
    }
    return data
}

@OptIn(ExperimentalResourceApi::class)
private suspend fun loadAnimationData(animationRes: LottieCompositionSpec.AnimationRes): JsonString =
    Res.readBytes("files/${animationRes.name}.json")
        .decodeToString()
        .let(::JsonString)

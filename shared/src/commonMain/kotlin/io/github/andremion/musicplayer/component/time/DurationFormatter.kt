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

package io.github.andremion.musicplayer.component.time

import kotlin.math.abs
import kotlin.time.Duration

fun Duration.format(): String {
    if (isInfinite()) return toString()
    val prefix = if (isNegative()) "-" else ""
    val absoluteRoundedSeconds = (abs(inWholeMilliseconds) + 500) / 1000
    val hours = absoluteRoundedSeconds / 3600
    val minutes = absoluteRoundedSeconds / 60 % 60
    val seconds = absoluteRoundedSeconds % 60
    return if (hours > 0) {
        "$prefix$hours:$minutes:${seconds.padded()}"
    } else {
        "$prefix$minutes:${seconds.padded()}"
    }
}

private fun Long.padded(): String =
    toString().padStart(length = 2, padChar = '0')

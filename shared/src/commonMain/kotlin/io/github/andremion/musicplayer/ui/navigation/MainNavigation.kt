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

@file:Suppress("INLINE_FROM_HIGHER_PLATFORM") // due to backStackEntry.query

package io.github.andremion.musicplayer.ui.navigation

import androidx.compose.runtime.Composable
import io.github.andremion.musicplayer.ui.discovery.DiscoveryScreen
import io.github.andremion.musicplayer.ui.player.PlayerScreen
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.query
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
fun MainNavigation() {
    val navigator = rememberNavigator()
    NavHost(
        navigator = navigator,
        initialRoute = "discovery"
    ) {
        scene(route = "discovery") {
            DiscoveryScreen(
                onNavigateToPlayer = { playlistId ->
                    navigator.navigate("player?playlistId=$playlistId")
                }
            )
        }
        scene(route = "player") { backStackEntry ->
            val playlistId = requireNotNull(backStackEntry.query<String>("playlistId"))
            PlayerScreen(playlistId)
        }
    }
}

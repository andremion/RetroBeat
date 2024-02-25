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

package io.github.andremion.musicplayer.data.di

import io.github.andremion.musicplayer.data.MusicRepositoryImpl
import io.github.andremion.musicplayer.data.api.deezer.DeezerApi
import io.github.andremion.musicplayer.data.api.deezer.DeezerHttpClientProvider
import io.github.andremion.musicplayer.domain.MusicRepository
import org.koin.dsl.module

object DataModule {
    val module = module {

        single { DeezerHttpClientProvider.provide() }

        factory {
            DeezerApi(
                httpClient = get()
            )
        }

        single<MusicRepository> {
            MusicRepositoryImpl(
                deezerApi = get()
            )
        }
    }
}

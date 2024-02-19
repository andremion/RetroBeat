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

package io.github.andremion.musicplayer.presentation

data class AsyncContent<Content>(
    val isLoading: Boolean = false,
    val content: Content? = null,
    val failure: Throwable? = null,
) {
    companion object {
        fun <Content> loading(): AsyncContent<Content> =
            AsyncContent(isLoading = true)

        fun <Content> success(content: Content): AsyncContent<Content> =
            AsyncContent(
                isLoading = false,
                content = content
            )

        fun <Content> failure(cause: Throwable): AsyncContent<Content> =
            AsyncContent(
                isLoading = false,
                failure = cause
            )
    }

    inline fun onLoading(action: () -> Unit): AsyncContent<Content> {
        if (isLoading) {
            action()
        }
        return this
    }

    inline fun onSuccess(action: (Content) -> Unit): AsyncContent<Content> {
        content?.let(action)
        return this
    }

    inline fun onFailure(action: (cause: Throwable) -> Unit): AsyncContent<Content> {
        failure?.let(action)
        return this
    }
}

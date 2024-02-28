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

@file:OptIn(ExperimentalResourceApi::class)

package io.github.andremion.musicplayer.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.utils.io.errors.IOException
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import retrobeat.shared.generated.resources.Res
import retrobeat.shared.generated.resources.error_retry_button
import retrobeat.shared.generated.resources.generic_error_message
import retrobeat.shared.generated.resources.generic_error_title
import retrobeat.shared.generated.resources.internet_connection_error_message
import retrobeat.shared.generated.resources.internet_connection_error_title

@Composable
fun ErrorView(
    cause: Throwable,
    onRetryClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
        )
        when (cause) {
            is IOException -> InternetConnectionError()
            else -> GenericError()
        }
        TextButton(onClick = onRetryClick) {
            Text(text = stringResource(Res.string.error_retry_button))
        }
    }
}

@Composable
private fun InternetConnectionError() {
    Text(
        text = stringResource(Res.string.internet_connection_error_title),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = stringResource(Res.string.internet_connection_error_message),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun GenericError() {
    Text(
        text = stringResource(Res.string.generic_error_title),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = stringResource(Res.string.generic_error_message),
        style = MaterialTheme.typography.bodyMedium
    )
}

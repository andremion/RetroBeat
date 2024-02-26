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

package io.github.andremion.musicplayer.ui.player

import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.andremion.musicplayer.component.time.format
import io.github.andremion.musicplayer.domain.entity.Playlist
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import retrobeat.shared.generated.resources.Res
import retrobeat.shared.generated.resources.player_playlist_album_art_content_description
import retrobeat.shared.generated.resources.player_playlist_options_content_description
import retrobeat.shared.generated.resources.player_playlist_track_count

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun Playlist(
    playlist: Playlist,
    selectedMusicId: String?,
    topBarPaddingTop: Dp,
    onMusicClick: (musicIndex: Int) -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(top = topBarPaddingTop),
                title = {
                    Column {
                        Text(
                            text = playlist.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(Res.string.player_playlist_track_count, playlist.musics.size),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.padding(end = 16.dp),
                        onClick = {

                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = stringResource(Res.string.player_playlist_options_content_description),
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(
                items = playlist.musics,
                key = { _, music -> music.id }
            ) { index, music ->
                val isSelected = music.id == selectedMusicId
                Card(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clip(CardShape)
                        .clickable { onMusicClick(index) },
                    shape = CardShape,
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.38f)
                        } else {
                            Color.Unspecified
                        }
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KamelImage(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(MaterialTheme.shapes.extraSmall),
                            resource = asyncPainterResource(music.album.picture.small),
                            contentDescription = stringResource(Res.string.player_playlist_album_art_content_description),
                            animationSpec = tween(),
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = music.title,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = music.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            modifier = Modifier.padding(end = 8.dp),
                            text = music.duration.format(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private val CardShape: CornerBasedShape
    @Composable get() = MaterialTheme.shapes.extraSmall.copy(
        topEnd = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp)
    )

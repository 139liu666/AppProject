
package com.peizhe.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.peizhe.todo.list.Item

@Composable
fun ItemDetailScreen(item: Item,viewModel: ItemsViewModel, onBack: () -> Unit) {
    val isMovie = item.title != null

    val favList by if(isMovie) viewModel.favMoviesStateFlow.collectAsStateWithLifecycle()
    else viewModel.favTVStateFlow.collectAsStateWithLifecycle()

    val watchedList by if(isMovie) viewModel.watchedMoviesStateFlow.collectAsStateWithLifecycle()
    else viewModel.watchedTVStateFlow.collectAsStateWithLifecycle()

    val isFav = favList.any { it.id == item.id }
    val isWatched = watchedList.any { it.id == item.id }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
            val imageUrl = "https://image.tmdb.org/t/p/w780${item.backdropPath ?: item.posterPath}"
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Button(
                onClick = onBack,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Back")
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.displayTitle,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(if (item.isMovie) "Movie" else "TV Series") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rating: ${item.voteAverage}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row {
                    IconButton(onClick = { viewModel.toggleFavorite(item) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { viewModel.toggleWatchlist(item) }) {
                        Icon(
                            imageVector = if (isWatched) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                            contentDescription = "Watched",
                            tint = if (isWatched) Color.Green else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Overview", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.overview.ifBlank { "No description available." },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
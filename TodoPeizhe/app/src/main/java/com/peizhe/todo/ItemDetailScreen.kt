
package com.peizhe.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
    var showRatingDialog by remember { mutableStateOf(false) }
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
                IconButton(onClick = { showRatingDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = "Rate",
                        tint = MaterialTheme.colorScheme.primary
                    )
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
    if (showRatingDialog) {
        RatingDialog(
            initialRating = 5.0,
            onDismiss = { showRatingDialog = false },
            onConfirm = { rating ->
                viewModel.rateMedia(item, rating)
                showRatingDialog = false
            }
        )
    }
}
@Composable
fun RatingDialog(
    initialRating: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var currentRating by remember { mutableStateOf(initialRating) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate this title") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${currentRating.toInt()} / 10",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        val score = i * 2.0
                        val isSelected = currentRating >= score

                        Icon(
                            imageVector = if (isSelected) Icons.Default.StarRate else Icons.Outlined.StarRate,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFFFFC107) else Color.Gray, // 金色或灰色
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    currentRating = score
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap a star to set score", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentRating) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
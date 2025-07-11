package com.hornet.movies.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.hornet.movies.data.model.meta.Genre
import com.hornet.movies.data.model.movie.Movie
import com.hornet.movies.data.model.movie.MovieDetails
import com.hornet.movies.presentation.list.ListIntent
import com.hornet.movies.presentation.list.ListViewState
import com.hornet.movies.presentation.core.Resource
import com.hornet.movies.ui.core.LabelText
import com.hornet.movies.ui.core.Spacer8
import com.hornet.movies.ui.core.Spacing

@Composable
fun ListScreen(
    state: ListViewState,
    onIntent: (ListIntent) -> Unit,
) {
    val lazyPagingItems = state.pager.flow.collectAsLazyPagingItems()
    val loadedGenreIdCount: Map<Genre, Int> = lazyPagingItems.itemSnapshotList
        .toList()
        .asSequence()
        .filterNotNull()
        .flatMap { movie ->
            movie.genre_ids.mapNotNull { genre ->
                val genreName = state.genresMap?.get(genre) ?: return@mapNotNull null
                genreName to 1
            }
        }
        .groupingBy { it.first }
        .fold(0) { acc, (_, count) -> acc + count }


    Column {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.dp8),
        ) {
            if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                item {
                    Text(
                        text = "Waiting for items to load from the backend",
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }

            items(count = lazyPagingItems.itemCount) { index ->
                val item = lazyPagingItems[index] ?: return@items
                val highlighted = state.selectedGenre?.id in item.genre_ids
                val expanded = item.id in state.expandedMovieIds
                val movieDetailsState = state.movieDetailsMap[item.id]
                MovieItem(
                    item,
                    highlighted,
                    expanded,
                    movieDetailsState,
                    onIntent
                )
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(
                horizontal = Spacing.dp16,
                vertical = Spacing.dp8,
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.dp8),
        ) {
            items(loadedGenreIdCount.toList()) { (genre, count) ->
                FilterChip(
                    onClick = { onIntent(ListIntent.GenreClicked(genre)) },
                    selected = state.selectedGenre == genre,
                    label = { Text("${genre.name} $count") },
                )
            }
        }
    }
}

@Composable
private fun MovieItem(
    movie: Movie,
    highlighted: Boolean,
    expanded: Boolean,
    movieDetailsState: Resource<MovieDetails>?,
    onIntent: (ListIntent) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onIntent(ListIntent.MovieClicked(movie))
            },
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) {
                Color.Green.copy(alpha = 0.2f)
            } else {
                Color.Unspecified
            }
        ),
    ) {
        Column {
            Box(Modifier.fillMaxWidth()) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    model = movie.backdrop,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = .7f))
                )
                AsyncImage(
                    modifier = Modifier
                        .padding(Spacing.dp8)
                        .height(104.dp)
                        .aspectRatio(.8f)
                        .align(Alignment.CenterStart),
                    model = movie.poster,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.dp16),
                verticalArrangement = Arrangement.spacedBy(Spacing.dp8),
            ) {
                LabelText(text = movie.title)
                Text(text = movie.overview, maxLines = 3)
                Text(text = "Rating: ${String.format("%.2f", movie.vote_average)}")
            }

            if (expanded && movieDetailsState != null) {
                HorizontalDivider()
                Spacer8()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.dp16),
                    verticalArrangement = Arrangement.spacedBy(Spacing.dp8),
                ) {
                    when (movieDetailsState) {
                        Resource.Loading -> CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )

                        Resource.Error -> Text("Couldn't load movie details")
                        is Resource.Loaded -> MovieDetailsSection(movieDetailsState.data)
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieDetailsSection(
    details: MovieDetails,
) {
    details.production_company?.let {
        it.logo?.let { logo ->
            AsyncImage(
                modifier = Modifier.size(40.dp),
                model = logo,
                contentDescription = "Production Company",
            )
        }
        Text(text = "Production Company: ${it.name}")
    }
    details.director?.let {
        Text(text = "Director: ${it.name}")
    }
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.dp8),
    ) {
        details.actors.forEach { actor ->
            AssistChip(
                onClick = {},
                label = { Text(actor.name) },
            )
        }
    }
}
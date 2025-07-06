package com.hornet.movies.presentation

import androidx.paging.PagingSource
import app.cash.turbine.test
import com.hornet.movies.data.MovieListPagingSource
import com.hornet.movies.data.MoviesService
import com.hornet.movies.data.model.meta.Genre
import com.hornet.movies.data.model.meta.Genres
import com.hornet.movies.data.model.meta.ProductionCompany
import com.hornet.movies.data.model.movie.Movie
import com.hornet.movies.data.model.movie.MovieDetails
import com.hornet.movies.presentation.list.ListIntent
import com.hornet.movies.presentation.list.ListViewModel
import com.hornet.movies.presentation.core.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ListViewModelTest : BaseTest() {

    private val moviesService: MoviesService = mockk {
        coEvery { getGenres() }.returns(Genres(MOCK_GENRES.values.toList()))
    }
    private val movieListPagingSource: MovieListPagingSource = mockk {
        coEvery { load(any()) }
            .returns(
                PagingSource.LoadResult.Page(
                    data = MOCK_MOVIES,
                    prevKey = null,
                    nextKey = null,
                )
            )
        coEvery { getRefreshKey(any()) }
            .returns(null)
    }

    private val viewModel: ListViewModel by lazy {
        ListViewModel(
            moviesService = moviesService,
            movieListPagingSource = movieListPagingSource,
        )
    }

    @Test
    fun `on init then fetch genres`() = runTest {
        viewModel.viewStateFlow.test {
            expectMostRecentItem()
            coVerify { moviesService.getGenres() }
        }
    }

    @Test
    fun `when movie clicked and movie details success then show details`() = runTest {
        viewModel.viewStateFlow.test {
            expectMostRecentItem()

            val movie = MOCK_MOVIES.first()
            val details = MOCK_MOVIE_DETAILS[movie.id]!!
            val detailsDeferred = CompletableDeferred<MovieDetails>()
            coEvery { moviesService.getMovieDetails(movie.id) }
                .coAnswers { detailsDeferred.await() }

            viewModel.onIntent(ListIntent.MovieClicked(movie))

            awaitItem().run {
                assertContains(expandedMovieIds, movie.id)
                val actualDetailsState = movieDetailsMap[movie.id]
                assertEquals(Resource.Loading, actualDetailsState)
            }

            coVerify { moviesService.getMovieDetails(movie.id) }

            detailsDeferred.complete(details)

            awaitItem().run {
                val actualDetailsState = movieDetailsMap[movie.id]
                assertEquals(Resource.Loaded(details), actualDetailsState)
            }

            viewModel.onIntent(ListIntent.MovieClicked(movie))

            awaitItem().run {
                assertFalse(movie.id in expandedMovieIds)
            }
        }
    }

    @Test
    fun `when movie clicked and movie details error then show error`() = runTest {
        viewModel.viewStateFlow.test {
            expectMostRecentItem()

            val movie = MOCK_MOVIES.first()
            val detailsDeferred = CompletableDeferred<MovieDetails>()
            coEvery { moviesService.getMovieDetails(movie.id) }
                .coAnswers { detailsDeferred.await() }

            viewModel.onIntent(ListIntent.MovieClicked(movie))

            awaitItem().run {
                assertContains(expandedMovieIds, movie.id)
                val actualDetailsState = movieDetailsMap[movie.id]
                assertEquals(Resource.Loading, actualDetailsState)
            }

            coVerify { moviesService.getMovieDetails(movie.id) }

            detailsDeferred.completeExceptionally(IllegalStateException())

            awaitItem().run {
                val actualDetailsState = movieDetailsMap[movie.id]
                assertEquals(Resource.Error, actualDetailsState)
            }
        }
    }

    @Test
    fun `when genre clicked then set selected genre`() = runTest {
        viewModel.viewStateFlow.test {
            expectMostRecentItem()

            val genre = MOCK_GENRES[1]!!
            viewModel.onIntent(ListIntent.GenreClicked(genre))

            awaitItem().run {
                assertEquals(genre, selectedGenre)
            }
        }
    }

    companion object {

        private val MOCK_GENRES = mapOf(
            1 to Genre(1, "Genre 1"),
            2 to Genre(2, "Genre 2"),
        )

        private val MOCK_MOVIES = listOf(
            Movie(
                id = 1,
                poster_path = null,
                backdrop_path = null,
                title = "Title 1",
                overview = "Overview 1",
                vote_average = 8.0,
                genre_ids = MOCK_GENRES.map { it.key }
            ),
            Movie(
                id = 2,
                poster_path = null,
                backdrop_path = null,
                title = "Title 2",
                overview = "Overview 2",
                vote_average = 6.0,
                genre_ids = MOCK_GENRES.map { it.key },
            ),
        )

        private val MOCK_MOVIE_DETAILS = mapOf(
            1 to MovieDetails(
                production_company = ProductionCompany(name = "Company 1")
            ),
            2 to MovieDetails(
                production_company = ProductionCompany(name = "Company 2")
            )
        )

    }

}
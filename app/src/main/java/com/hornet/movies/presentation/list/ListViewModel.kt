package com.hornet.movies.presentation.list

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.hornet.movies.data.MoviesService
import com.hornet.movies.data.MovieListPagingSource
import com.hornet.movies.presentation.core.BaseViewModel
import com.hornet.movies.presentation.core.TopBarViewState


class ListViewModel(
    private val moviesService: MoviesService,
    private val movieListPagingSource: MovieListPagingSource,
) : BaseViewModel<
        ListArgs,
        ListIntent,
        ListModelState,
        ListViewState,
        ListNavigation,
        >(
    initialModelState = ListModelState.initial(
        pager = Pager(PagingConfig(20)) { movieListPagingSource },
    ),
), ListContract {

    override fun mapViewState(state: ListModelState): ListViewState = ListViewState(
        commonState = state.commonState.toViewState(
            topBarViewState = TopBarViewState(
                title = "Movies",
                backEnabled = false,
            )
        ),
        pager = state.pager,
        expandedMovieIds = state.expandedMovieIds,
        movieDetailsMap = state.movieDetailsMap,
    )

    override suspend fun handleIntent(state: ListModelState, intent: ListIntent) {
        when (intent) {
            is ListIntent.MovieClicked -> {
                val movieId = intent.movie.id
                val expand = movieId !in state.expandedMovieIds
                if (expand && state.movieDetailsMap[movieId] !is Resource.Loaded) {
                    loadMovieDetails(movieId)
                }
                updateState {
                    it.copy(
                        expandedMovieIds = if (expand) {
                            it.expandedMovieIds + movieId
                        } else {
                            it.expandedMovieIds - movieId
                        },
                    )
                }
            }
        }
    }

    private suspend fun loadMovieDetails(movieId: Int) {
        updateState {
            it.copy(
                movieDetailsMap = it.movieDetailsMap + (movieId to Resource.Loading)
            )
        }

        val newMovieDetailsMap = try {
            val details = moviesService.getMovieDetails(movieId)
            modelState.movieDetailsMap + (movieId to Resource.Loaded(details))
        } catch (ex: Exception) {
            modelState.movieDetailsMap + (movieId to Resource.Error)
        }

        updateState { it.copy(movieDetailsMap = newMovieDetailsMap) }
    }

}
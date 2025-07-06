package com.hornet.movies.presentation.list

import androidx.paging.Pager
import com.hornet.movies.data.model.meta.Genre
import com.hornet.movies.data.model.movie.Movie
import com.hornet.movies.data.model.movie.MovieDetails
import com.hornet.movies.presentation.core.Args
import com.hornet.movies.presentation.core.CommonModelState
import com.hornet.movies.presentation.core.CommonViewState
import com.hornet.movies.presentation.core.Intent
import com.hornet.movies.presentation.core.ModelStateWithCommonState
import com.hornet.movies.presentation.core.Navigation
import com.hornet.movies.presentation.core.ViewModel
import com.hornet.movies.presentation.core.ViewStateWithCommonState

interface ListContract : ViewModel<
        ListArgs,
        ListIntent,
        ListModelState,
        ListViewState,
        ListNavigation,
        >

data object ListArgs : Args

sealed interface ListIntent : Intent {
    data class MovieClicked(val movie: Movie) : ListIntent
    data class GenreClicked(val genre: Genre) : ListIntent
}

data class ListViewState(
    override val commonState: CommonViewState,
    val pager: Pager<Int, Movie>,
    val genresMap: Map<Int, Genre>?,
    val selectedGenre: Genre?,
    val expandedMovieIds: Set<Int>,
    val movieDetailsMap: Map<Int, Resource<MovieDetails>>,
) : ViewStateWithCommonState

data class ListModelState(
    override val commonState: CommonModelState,
    val pager: Pager<Int, Movie>,
    val genresMap: Map<Int, Genre>?,
    val selectedGenre: Genre?,
    val expandedMovieIds: Set<Int>,
    val movieDetailsMap: Map<Int, Resource<MovieDetails>>,
) : ModelStateWithCommonState<ListModelState> {
    override fun copyCommon(commonState: CommonModelState): ListModelState =
        copy(commonState = commonState)

    companion object {
        fun initial(pager: Pager<Int, Movie>) = ListModelState(
            commonState = CommonModelState(),
            pager = pager,
            genresMap = null,
            selectedGenre = null,
            expandedMovieIds = emptySet(),
            movieDetailsMap = emptyMap(),
        )
    }
}

sealed interface ListNavigation : Navigation

sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Loaded<out T>(val data: T) : Resource<T>
    data object Error : Resource<Nothing>
}
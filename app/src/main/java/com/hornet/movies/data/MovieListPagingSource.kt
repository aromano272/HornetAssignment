package com.hornet.movies.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hornet.movies.data.model.movie.Movie

class MovieListPagingSource(
    private val moviesService: MoviesService,
) : PagingSource<Int, Movie>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return try {
            val pageNumber = params.key ?: 1
            val response = moviesService.getTopMovies(pageNumber)
            val totalMovies = response.results.size
            val filteredMovies = response.results.filter {
                it.vote_average >= 7.0
            }
            val nextPageNumber = (pageNumber + 1)
                .takeIf { totalMovies != 0 && totalMovies == filteredMovies.size }
            LoadResult.Page(
                data = filteredMovies,
                prevKey = null,
                nextKey = nextPageNumber,
            )
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

}
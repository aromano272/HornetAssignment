package com.hornet.movies.data.model.movie

data class MovieResults(
    val page: Int,
    val results: List<Movie>,
)
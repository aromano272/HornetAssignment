package com.hornet.movies.data.model.meta

data class Genres(
    val genres: List<Genre>,
)

data class Genre(
    val id: Int = 0,
    val name: String,
)
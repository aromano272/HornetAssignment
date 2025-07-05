package com.hornet.movies.util

fun String?.pathToUrl(): String {
    return if (this.isNullOrBlank()) "" else "https://image.tmdb.org/t/p/w500$this"
}
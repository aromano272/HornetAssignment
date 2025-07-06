package com.hornet.movies.presentation.core

sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Loaded<out T>(val data: T) : Resource<T>
    data object Error : Resource<Nothing>
}
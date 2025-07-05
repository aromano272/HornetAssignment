package com.hornet.movies.di

import com.hornet.movies.data.MoviesService
import com.hornet.movies.data.MovieListPagingSource
import com.hornet.movies.presentation.list.ListViewModel
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<Json> {
        Json {
            explicitNulls = false
            encodeDefaults = true
            coerceInputValues = true
            ignoreUnknownKeys = true
        }
    }

}

val presentationModule = module {

    viewModel {
        ListViewModel(
            moviesService = get(),
            movieListPagingSource = get(),
        )
    }

}

val dataModule = module {

    single<MoviesService> {
        MoviesService.getInstance()
    }

    factory {
        MovieListPagingSource(
            moviesService = get()
        )
    }

}


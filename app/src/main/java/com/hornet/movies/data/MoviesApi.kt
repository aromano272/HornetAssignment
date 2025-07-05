package com.hornet.movies.data

import com.hornet.movies.data.model.meta.Genres
import com.hornet.movies.data.model.movie.MovieDetails
import com.hornet.movies.data.model.movie.MovieResults
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MoviesApi {
    @GET(value = "discover/movie")
    suspend fun discover(
        @Query("page") page: Int,
        @Query("sort_by") sortBy: String = "vote_count.desc",
        @Query("primary_release_date.gte") releaseDateMin: String = "1980-09-12",
        @Query("with_original_language") originalLanguage: String = "en",
        @Query("include_video") includeVideo: Boolean = false,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("language") language: String = "en-US",
    ): MovieResults


    @GET(value = "https://api.themoviedb.org/3/movie/{movie_id}?append_to_response=credits")
    suspend fun details(@Path("movie_id") id: Int): MovieDetails

    @GET(value = "https://api.themoviedb.org/3/genre/movie/list?language=en")
    suspend fun genres(): Genres
}
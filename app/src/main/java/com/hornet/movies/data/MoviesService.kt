package com.hornet.movies.data

import android.net.Uri
import android.os.Build
import com.hornet.movies.BuildConfig
import com.hornet.movies.data.model.meta.Genres
import com.hornet.movies.data.model.movie.MovieDetails
import com.hornet.movies.data.model.movie.MovieResults
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MoviesService private constructor() {
    /**
     * Gets a list of top rates movies
     *
     * @param page the page number, starting at 1
     * @return MovieResults containing results, a list of Movie objects
     */
    suspend fun getTopMovies(page: Int): MovieResults {
        return apiService.discover(page)
    }

    /**
     * Gets details about a movie
     *
     * @param id the id of the movie
     * @return MovieDetails containing director, actors, and production company info
     */
    suspend fun getMovieDetails(id: Int): MovieDetails {
        return apiService.details(id)
    }

    /**
     * Gets map of genres to ids
     *
     * @return Genres containing list of id -> genre mappings
     */
    suspend fun getGenres(): Genres {
        return apiService.genres()
    }

    /**
     *
     */
    private val baseUrl: String = "https://api.themoviedb.org/3/"
    private val headerValues: HashMap<String, String> = HashMap()
    private lateinit var apiService: MoviesApi

    private fun launchService() {
        configureHeaders()
        apiService = createApiService(getRetrofitInstance())
    }

    private fun configureHeaders() {
        addHeader(
            "X-Device-Name",
            String.format(
                "%s %s",
                encodeForHeaderValue(Build.MANUFACTURER),
                encodeForHeaderValue(Build.MODEL)
            )
        )
        addHeader("X-Device-Version", "Android ${Build.VERSION.RELEASE}")
        addHeader("Accept-Language", Locale.getDefault().language)
        addHeader("Accept", "application/json")
        addHeader("Authorization", "Bearer ${BuildConfig.TMDB_KEY}")
    }

    private fun addHeader(key: String, value: String) {
        headerValues[key] = value
    }

    private fun encodeForHeaderValue(rawValue: String): String {
        return Uri.encode(rawValue, " ")
    }

    private fun createApiService(retrofit: Retrofit): MoviesApi {
        return retrofit.create(MoviesApi::class.java)
    }

    private fun getRetrofitInstance(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .addInterceptor(APIHeaderInterceptor())

        val debugLoggingInterceptor = HttpLoggingInterceptor()
        debugLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient.addInterceptor(debugLoggingInterceptor)

        return Retrofit.Builder().baseUrl(baseUrl)
            .addParsingFactories()
            .client(okHttpClient.build())
            .build()
    }

    private fun Retrofit.Builder.addParsingFactories(): Retrofit.Builder {
        addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                    .add(KotlinJsonAdapterFactory())
                    .build()
            )
        )
        return this
    }

    inner class APIHeaderInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            with(chain.request().newBuilder()) {
                headerValues.keys.forEach { key ->
                    headerValues[key]?.let { value ->
                        addHeader(key, value)
                    }
                }
                return chain.proceed(this.build())
            }
        }
    }

    companion object {
        fun getInstance(): MoviesService {
            return MoviesService().apply {
                launchService()
            }
        }
    }
}
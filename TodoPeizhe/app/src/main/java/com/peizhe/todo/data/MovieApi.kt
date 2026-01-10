package com.peizhe.todo.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object MovieApi {
    var TOKEN:String? ="eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhNzZjMTdlM2Q3NGUyNDk0MTJlMjliODZjYzQwN2Q2YiIsIm5iZiI6MTc2NTgwODEwMy44NjYsInN1YiI6IjY5NDAxN2U3YmI2ZTZlYjczZjk5OWNkNyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.VXZFZhbHRwk6WsYHy4qOGVFbdJdygYn5XKDB_5IMV5M"
    var sessionId: String? = null
    private val retrofit by lazy {
        // client HTTP
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url

                val newUrl = originalUrl.newBuilder().apply {
                    sessionId?.let {
                        addQueryParameter("session_id", it)
                    }
                }.build()
                val newRequest = chain.request().newBuilder()
                    .url(newUrl)
                    .addHeader("Authorization", "Bearer $TOKEN")
                    .build()
                chain.proceed(newRequest)
            }
            .build()

        val jsonSerializer = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }

        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/")
            .client(okHttpClient)
            .addConverterFactory(jsonSerializer.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    val moviesWebService: MoviesWebService by lazy {
        retrofit.create(MoviesWebService::class.java)
    }
}
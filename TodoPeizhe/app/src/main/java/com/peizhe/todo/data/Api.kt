package com.peizhe.todo.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import kotlin.jvm.java

object Api {
    private const val TOKEN = "fc6a9f9f6fff2ff159d43db4d0817970690d5946"

    private val retrofit by lazy {
        // client HTTP
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()
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

        // instance retrofit pour implémenter les webServices:
        Retrofit.Builder()
            .baseUrl("https://api.todoist.com/")
            .client(okHttpClient)
            .addConverterFactory(jsonSerializer.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    val userWebService: UserWebService by lazy {
        retrofit.create(UserWebService::class.java)
    }
    val taskWebService: TaskWebService by lazy {
        retrofit.create(TaskWebService::class.java)
    }
}
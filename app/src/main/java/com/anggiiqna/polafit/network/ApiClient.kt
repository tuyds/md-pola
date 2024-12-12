package com.anggiiqna.polafit.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://polafit-cloud-computing-357126485159.asia-southeast2.run.app/"
    private const val BASE_URL_ML = "https://polafit-machine-learning-357126485159.asia-southeast2.run.app/"

    fun create(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun createML(): ApiService {
        val httpClient = OkHttpClient.Builder()
            .callTimeout(2, TimeUnit.MINUTES)  // Set timeout to 2 minutes
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL_ML)
            .client(httpClient)  // Use the custom client with a timeout
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}


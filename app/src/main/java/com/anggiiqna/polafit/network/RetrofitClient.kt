package com.anggiiqna.polafit.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://polafit-machine-learning-357126485159.asia-southeast2.run.app/"

    fun create(): ApiService {
        val httpClient = OkHttpClient.Builder()
            .callTimeout(2, TimeUnit.MINUTES)  // Set timeout to 2 minutes
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)  // Use the custom client with a timeout
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

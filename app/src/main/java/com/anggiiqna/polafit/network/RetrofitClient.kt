package com.anggiiqna.polafit.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody
import retrofit2.Call

object RetrofitClient {
    private const val BASE_URL = "https://polafit-machine-learning-357126485159.asia-southeast2.run.app/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}


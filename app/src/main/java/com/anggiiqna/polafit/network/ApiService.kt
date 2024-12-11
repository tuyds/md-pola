package com.anggiiqna.polafit.network

import com.anggiiqna.polafit.network.datamodel.LoginRequest
import com.anggiiqna.polafit.network.datamodel.LoginResponse
import com.anggiiqna.polafit.network.datamodel.RegisterRequest
import com.anggiiqna.polafit.network.datamodel.RegisterResponse
import com.anggiiqna.polafit.network.datamodel.UserRequest
import com.anggiiqna.polafit.network.datamodel.UserResponse
import retrofit2.http.Path
import retrofit2.http.Part
import retrofit2.http.Multipart
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.Call

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("auth/user/{id}")
    suspend fun getUserById(@Path("id") id: String): UserResponse

    @Multipart
    @PUT("auth/user/{id}")
    suspend fun updateUserProfileWithImage(
        @Path("id") id: String,
        @Part username: MultipartBody.Part,
        @Part email: MultipartBody.Part,
        @Part phone: MultipartBody.Part,
        @Part image: MultipartBody.Part
    ): UserRequest

    @Multipart
    @PUT("auth/user/{id}")
    suspend fun updateUserProfileWithoutImage(
        @Path("id") id: String,
        @Part username: MultipartBody.Part,
        @Part email: MultipartBody.Part,
        @Part phone: MultipartBody.Part,
    ): UserRequest

    @Multipart
    @POST("/predict_food")
    fun predictFood(@Part file: MultipartBody.Part): Call<ScanResponse>


}

package com.anggiiqna.polafit.network.datamodel

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message")
    val message: Boolean,
    @SerializedName("token")
    val token: String,
    val user: UserResponse
)
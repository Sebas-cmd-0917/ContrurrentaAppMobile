package com.construrrenta.app.data.api

import com.construrrenta.app.data.model.LoginRequest
import com.construrrenta.app.data.model.LoginResponse
import com.construrrenta.app.data.model.RegisterRequest
import com.construrrenta.app.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserResponse>
}
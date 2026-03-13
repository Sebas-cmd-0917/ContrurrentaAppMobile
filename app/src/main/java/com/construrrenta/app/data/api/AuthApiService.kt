package com.construrrenta.app.data.api

import com.construrrenta.app.data.model.LoginRequest
import com.construrrenta.app.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    // Asegúrate de que esta sea la ruta exacta de tu Spring Boot
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
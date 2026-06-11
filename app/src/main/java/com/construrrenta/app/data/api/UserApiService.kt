package com.construrrenta.app.data.api

import com.construrrenta.app.data.model.CreateUserRequest
import com.construrrenta.app.data.model.UpdateUserRequest
import com.construrrenta.app.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface UserApiService {

    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<UserResponse>

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserResponse>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Void>
}

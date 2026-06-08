package com.construrrenta.app.data.api

import com.construrrenta.app.data.model.ToolResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ToolApiService {
    @GET("api/v1/tools")
    suspend fun getAllTools(): Response<List<ToolResponse>>

    @GET("api/v1/tools/{id}")
    suspend fun getToolById(@Path("id") id: String): Response<ToolResponse>
}
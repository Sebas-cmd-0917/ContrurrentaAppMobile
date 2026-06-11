package com.construrrenta.app.data.api

import com.construrrenta.app.data.model.ToolRequest
import com.construrrenta.app.data.model.ToolResponse
import retrofit2.Response
import retrofit2.http.*

interface ToolApiService {

    @GET("tools")
    suspend fun getAllTools(): Response<List<ToolResponse>>

    @GET("tools/{id}")
    suspend fun getToolById(@Path("id") id: String): Response<ToolResponse>

    @GET("tools/provider/{providerId}")
    suspend fun getToolsByProvider(@Path("providerId") providerId: String): Response<List<ToolResponse>>

    @GET("tools/search")
    suspend fun searchTools(@Query("name") name: String): Response<List<ToolResponse>>

    @POST("tools")
    suspend fun createTool(@Body request: ToolRequest): Response<ToolResponse>

    @PUT("tools/{id}")
    suspend fun updateTool(@Path("id") id: String, @Body request: ToolRequest): Response<ToolResponse>

    @DELETE("tools/{id}")
    suspend fun deleteTool(@Path("id") id: String): Response<Void>
}
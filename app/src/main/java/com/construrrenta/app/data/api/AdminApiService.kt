package com.construrrenta.app.data.api

import com.construrrenta.app.data.model.DamageReportResponse
import com.construrrenta.app.data.model.DashboardStats
import com.construrrenta.app.data.model.PaymentResponse
import com.construrrenta.app.data.model.ToolResponse
import com.construrrenta.app.data.model.UserResponse
import retrofit2.Response
import retrofit2.http.GET

interface AdminApiService {

    @GET("admin/reports/dashboard")
    suspend fun getDashboardStats(): Response<DashboardStats>

    @GET("admin/reports/top-tools")
    suspend fun getTopTools(): Response<List<ToolResponse>>

    @GET("admin/reports/top-users")
    suspend fun getTopUsers(): Response<List<UserResponse>>

    @GET("admin/payments")
    suspend fun getAllPayments(): Response<List<PaymentResponse>>

    @GET("damage-reports")
    suspend fun getAllDamageReports(): Response<List<DamageReportResponse>>
}

package com.construrrenta.app.data.api

import com.construrrenta.app.data.model.BookingRequest
import com.construrrenta.app.data.model.BookingResponse
import com.construrrenta.app.data.model.PaymentConfirmationRequest
import com.construrrenta.app.data.model.ReturnRequest
import retrofit2.Response
import retrofit2.http.*

interface BookingApiService {

    @POST("bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingResponse>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: String): Response<BookingResponse>

    @GET("bookings/my-history")
    suspend fun getMyHistory(): Response<List<BookingResponse>>

    @GET("bookings/user/{userId}")
    suspend fun getBookingsByUser(@Path("userId") userId: String): Response<List<BookingResponse>>

    @GET("bookings")
    suspend fun getAllBookings(): Response<List<BookingResponse>>

    @GET("bookings/provider/{providerId}")
    suspend fun getProviderBookings(@Path("providerId") providerId: String): Response<List<BookingResponse>>

    @POST("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: String): Response<Void>

    @POST("bookings/{id}/confirm-payment")
    suspend fun confirmPayment(
        @Path("id") id: String,
        @Body request: PaymentConfirmationRequest
    ): Response<Void>

    @POST("bookings/{id}/return")
    suspend fun returnTool(
        @Path("id") id: String,
        @Body request: ReturnRequest
    ): Response<Void>

    @POST("bookings/{id}/report-arrival-issue")
    suspend fun reportArrivalIssue(
        @Path("id") id: String,
        @Body request: ReturnRequest
    ): Response<Void>

    @POST("bookings/{id}/approve")
    suspend fun approveBooking(@Path("id") id: String): Response<Void>

    @POST("bookings/{id}/reject")
    suspend fun rejectBooking(@Path("id") id: String): Response<Void>
}
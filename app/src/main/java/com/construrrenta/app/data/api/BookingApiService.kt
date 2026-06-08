package com.construrrenta.app.data.api

import com.construrrenta.app.data.model.BookingRequest
import com.construrrenta.app.data.model.BookingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BookingApiService {
    @POST("api/v1/bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingResponse>
}
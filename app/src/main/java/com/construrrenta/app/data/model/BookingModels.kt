package com.construrrenta.app.data.model

data class BookingRequest(
    val userId: String,
    val toolId: String,
    val startDate: String,
    val endDate: String
)

data class BookingResponse(
    val id: String,
    val userId: String,
    val tool: ToolResponse,
    val paymentId: String?,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String
)
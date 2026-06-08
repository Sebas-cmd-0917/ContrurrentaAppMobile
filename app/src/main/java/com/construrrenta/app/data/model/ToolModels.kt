package com.construrrenta.app.data.model

data class ToolResponse(
    val id: String,
    val name: String,
    val description: String,
    val pricePerDay: Double,
    val imageUrl: String,
    val status: String,
    val providerId: String,
    val stock: Int
)
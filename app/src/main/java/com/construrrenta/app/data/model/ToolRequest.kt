package com.construrrenta.app.data.model

data class ToolRequest(
    val name: String,
    val description: String,
    val pricePerDay: Double,
    val imageUrl: String?,
    val providerId: String,
    val stock: Int = 1
)

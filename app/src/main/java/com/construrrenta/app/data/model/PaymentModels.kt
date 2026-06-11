package com.construrrenta.app.data.model

// Request para confirmar pago
data class PaymentConfirmationRequest(
    val paymentId: String
)

// Request para devolver herramienta / reportar daño
data class ReturnRequest(
    val withDamage: Boolean = false,
    val damageDescription: String? = null,
    val repairCost: Double? = null
)

// Respuesta de reporte de daño
data class DamageReportResponse(
    val id: String,
    val description: String,
    val repairCost: Double?,
    val reportDate: String,
    val isRepaired: Boolean,
    val bookingId: String
)

// Respuesta de pago
data class PaymentResponse(
    val id: String,
    val amount: Double,
    val paymentDate: String,
    val method: String, // CREDIT_CARD, PAYPAL, TRANSFER, CASH
    val status: String, // PENDING, COMPLETED, FAILED, REFUNDED
    val bookingId: String
)

// Estadísticas del dashboard (Admin)
data class DashboardStats(
    val totalUsers: Long,
    val activeBookings: Long,
    val totalRevenue: Double
)

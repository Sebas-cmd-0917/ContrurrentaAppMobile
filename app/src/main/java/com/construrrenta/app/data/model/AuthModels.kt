package com.construrrenta.app.data.model

// Lo que la app móvil envía al backend
data class LoginRequest(
    val email: String,
    val contrasena: String
)

// Lo que el backend responde si el login es exitoso
data class LoginResponse(
    val token: String,
    val role: String, // Opcional, si tu backend devuelve el rol (ADMIN, CUSTOMER...)
    val message: String? = null
)
package com.construrrenta.app.data.model

// Lo que la app móvil envía al backend para login
data class LoginRequest(
    val email: String,
    val password: String
)

// Lo que el backend responde si el login es exitoso (JWT)
data class LoginResponse(
    val accessToken: String,
    val tokenType: String? = "Bearer"
)

// Lo que la app envía para registrar un nuevo usuario
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)
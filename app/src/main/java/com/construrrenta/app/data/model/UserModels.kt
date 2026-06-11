package com.construrrenta.app.data.model

// Respuesta del backend al obtener un usuario
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String // ADMIN, PROVIDER, CUSTOMER
)

// Request para crear usuario (Admin)
data class CreateUserRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: String
)

// Request para actualizar usuario
data class UpdateUserRequest(
    val firstName: String?,
    val lastName: String?,
    val password: String?
)

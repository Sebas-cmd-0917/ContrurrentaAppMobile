package com.construrrenta.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.construrrenta.app.data.api.AuthApiService
import com.construrrenta.app.data.local.SessionManager
import com.construrrenta.app.data.model.LoginRequest
import com.construrrenta.app.data.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class LoginSuccess(val role: String) : AuthUiState()
    object RegisterSuccess : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authApiService: AuthApiService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Email y contraseña son obligatorios")
            return
        }
        if (!email.contains("@")) {
            _uiState.value = AuthUiState.Error("Email inválido")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authApiService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    sessionManager.saveAuthToken(loginResponse.accessToken)
                    val role = sessionManager.getRole() ?: "CUSTOMER"
                    _uiState.value = AuthUiState.LoginSuccess(role)
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        403 -> "Acceso denegado"
                        else -> "Error del servidor: ${response.code()}"
                    }
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    fun register(firstName: String, lastName: String, email: String, password: String) {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Todos los campos son obligatorios")
            return
        }
        if (!email.contains("@")) {
            _uiState.value = AuthUiState.Error("Email inválido")
            return
        }
        if (password.length < 4) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 4 caracteres")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authApiService.register(
                    RegisterRequest(email, password, firstName, lastName)
                )
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.RegisterSuccess
                } else {
                    val errorMsg = when (response.code()) {
                        409 -> "El email ya está registrado"
                        400 -> "Datos inválidos"
                        else -> "Error del servidor: ${response.code()}"
                    }
                    _uiState.value = AuthUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Error de conexión: ${e.localizedMessage}")
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.value = AuthUiState.Idle
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}

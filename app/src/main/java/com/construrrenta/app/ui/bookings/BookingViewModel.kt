package com.construrrenta.app.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.construrrenta.app.data.api.BookingApiService
import com.construrrenta.app.data.model.BookingResponse
import com.construrrenta.app.data.model.PaymentConfirmationRequest
import com.construrrenta.app.data.model.ReturnRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookingUiState {
    object Loading : BookingUiState()
    data class Success(val bookings: List<BookingResponse>) : BookingUiState()
    object Empty : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

sealed class BookingActionState {
    object Idle : BookingActionState()
    object Loading : BookingActionState()
    data class Success(val message: String) : BookingActionState()
    data class Error(val message: String) : BookingActionState()
}

class BookingViewModel(private val apiService: BookingApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Loading)
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<BookingActionState>(BookingActionState.Idle)
    val actionState: StateFlow<BookingActionState> = _actionState.asStateFlow()

    fun fetchMyHistory() {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                val response = apiService.getMyHistory()
                if (response.isSuccessful && response.body() != null) {
                    val bookings = response.body()!!
                    _uiState.value = if (bookings.isEmpty()) BookingUiState.Empty else BookingUiState.Success(bookings)
                } else {
                    _uiState.value = BookingUiState.Error("Error al cargar reservas: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = BookingUiState.Error("Fallo de red: ${e.message}")
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _actionState.value = BookingActionState.Loading
            try {
                val response = apiService.cancelBooking(bookingId)
                if (response.isSuccessful) {
                    _actionState.value = BookingActionState.Success("Reserva cancelada")
                    fetchMyHistory()
                } else {
                    _actionState.value = BookingActionState.Error("No se pudo cancelar: ${response.code()}")
                }
            } catch (e: Exception) {
                _actionState.value = BookingActionState.Error("Error: ${e.message}")
            }
        }
    }

    fun confirmPayment(bookingId: String, paymentMethod: String) {
        viewModelScope.launch {
            _actionState.value = BookingActionState.Loading
            try {
                val paymentId = "PAY-${System.currentTimeMillis()}-$paymentMethod"
                val response = apiService.confirmPayment(bookingId, PaymentConfirmationRequest(paymentId))
                if (response.isSuccessful) {
                    _actionState.value = BookingActionState.Success("Pago confirmado")
                    fetchMyHistory()
                } else {
                    _actionState.value = BookingActionState.Error("Error al confirmar pago: ${response.code()}")
                }
            } catch (e: Exception) {
                _actionState.value = BookingActionState.Error("Error: ${e.message}")
            }
        }
    }

    fun reportArrivalIssue(bookingId: String, description: String) {
        viewModelScope.launch {
            _actionState.value = BookingActionState.Loading
            try {
                val request = ReturnRequest(withDamage = true, damageDescription = description, repairCost = 0.0)
                val response = apiService.reportArrivalIssue(bookingId, request)
                if (response.isSuccessful) {
                    _actionState.value = BookingActionState.Success("Reporte enviado")
                    fetchMyHistory()
                } else {
                    _actionState.value = BookingActionState.Error("Error al reportar: ${response.code()}")
                }
            } catch (e: Exception) {
                _actionState.value = BookingActionState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = BookingActionState.Idle
    }
}

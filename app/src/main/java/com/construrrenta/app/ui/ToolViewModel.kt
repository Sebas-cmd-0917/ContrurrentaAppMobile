package com.construrrenta.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.construrrenta.app.data.api.ToolApiService
import com.construrrenta.app.data.model.ToolResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ToolUiState {
    object Loading : ToolUiState()
    data class Success(val tools: List<ToolResponse>) : ToolUiState()
    data class Error(val message: String) : ToolUiState()
}

class ToolViewModel(private val apiService: ToolApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<ToolUiState>(ToolUiState.Loading)
    val uiState: StateFlow<ToolUiState> = _uiState.asStateFlow()

    init {
        fetchTools()
    }

    private fun fetchTools() {
        viewModelScope.launch {
            _uiState.value = ToolUiState.Loading
            try {
                val response = apiService.getAllTools()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ToolUiState.Success(response.body()!!)
                } else {
                    _uiState.value = ToolUiState.Error("Error al cargar herramientas: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ToolUiState.Error("Fallo de red: ${e.message}")
            }
        }
    }
}
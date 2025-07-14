package com.dollargeneral.palletmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollargeneral.palletmanager.data.entities.ActiveAssignment
import com.dollargeneral.palletmanager.data.repository.PalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel following Android best practices
 * - Uses Hilt for dependency injection
 * - Repository pattern
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PalletRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // Active assignments from database
    val activeAssignments: StateFlow<List<ActiveAssignment>> = repository
        .getActiveAssignments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Assignment count
    val assignmentCount: StateFlow<Int> = repository
        .getActiveAssignmentCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    /**
     * Mark a pallet assignment as delivered
     */
    fun markAsDelivered(assignmentId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.markAsDelivered(assignmentId)
                .onSuccess {
                    showMessage("Pallet marked as delivered!")
                }
                .onFailure { error ->
                    showError("Failed to mark as delivered: ${error.message}")
                }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    /**
     * Delete a pallet assignment
     */
    fun deleteAssignment(assignmentId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.deleteAssignment(assignmentId)
                .onSuccess {
                    showMessage("Assignment deleted")
                }
                .onFailure { error ->
                    showError("Failed to delete assignment: ${error.message}")
                }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    /**
     * Clean up old delivered assignments
     */
    fun cleanupOldDeliveries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.cleanupOldDeliveries()
                .onSuccess {
                    showMessage("Old deliveries cleaned up")
                }
                .onFailure { error ->
                    showError("Failed to cleanup: ${error.message}")
                }
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
    
    /**
     * Clear any displayed message
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            isError = false
        )
    }
    
    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            message = message,
            isError = false
        )
    }
    
    private fun showError(error: String) {
        _uiState.value = _uiState.value.copy(
            message = error,
            isError = true
        )
    }

    /**
     * Lookup check digit for a given station number.
     */
    suspend fun lookupStation(stationNumber: String): String? {
        return repository.getCheckDigitForStation(stationNumber)
    }
}

/**
 * UI State for the main screen
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

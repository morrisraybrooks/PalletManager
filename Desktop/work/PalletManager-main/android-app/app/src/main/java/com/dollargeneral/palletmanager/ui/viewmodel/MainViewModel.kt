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

    /**
     * Get reactive Flow for check digit lookup that updates automatically
     * when station data changes in the database
     */
    fun getCheckDigitFlow(stationNumber: String): Flow<String?> {
        return if (stationNumber.isBlank()) {
            flowOf(null)
        } else {
            repository.getCheckDigitForStationFlow(stationNumber)
        }
    }

    /**
     * Get all stations as a reactive Flow for caching and real-time updates
     */
    val allStations: StateFlow<List<com.dollargeneral.palletmanager.data.entities.StationLookup>> = repository
        .getAllStationsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current lookup job to cancel previous lookups
    private var currentLookupJob: kotlinx.coroutines.Job? = null

    /**
     * Update station input and trigger reactive check digit lookup
     */
    fun updateStationInput(input: String) {
        // Cancel any previous lookup
        currentLookupJob?.cancel()

        _uiState.value = _uiState.value.copy(
            stationInput = input,
            isLookingUp = input.isNotBlank()
        )

        // Start reactive lookup for the new input
        if (input.isNotBlank()) {
            currentLookupJob = viewModelScope.launch {
                getCheckDigitFlow(input).collect { checkDigit ->
                    // Only update if this is still the current input
                    if (_uiState.value.stationInput == input) {
                        _uiState.value = _uiState.value.copy(
                            checkDigitResult = checkDigit,
                            isLookingUp = false
                        )
                    }
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(
                checkDigitResult = null,
                isLookingUp = false
            )
        }
    }

    /**
     * Clear station input and check digit result
     */
    fun clearStationInput() {
        currentLookupJob?.cancel()
        _uiState.value = _uiState.value.copy(
            stationInput = "",
            checkDigitResult = null,
            isLookingUp = false
        )
    }
}

/**
 * UI State for the main screen
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val stationInput: String = "",
    val checkDigitResult: String? = null,
    val isLookingUp: Boolean = false
)

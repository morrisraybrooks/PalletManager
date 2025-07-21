package com.dollargeneral.palletmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollargeneral.palletmanager.data.entities.StationLookup
import com.dollargeneral.palletmanager.data.repository.PalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseViewerViewModel @Inject constructor(
    private val repository: PalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatabaseViewerUiState())
    val uiState: StateFlow<DatabaseViewerUiState> = _uiState.asStateFlow()

    // All stations from the database
    val stations: StateFlow<List<StationLookup>> = repository.getAllStations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Assignment count
    val assignmentCount: StateFlow<Int> = repository.getActiveAssignmentCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        // Update assignment count in UI state
        viewModelScope.launch {
            assignmentCount.collect { count ->
                _uiState.value = _uiState.value.copy(assignmentCount = count)
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Data is automatically refreshed through the Flow
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to refresh data: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * UI State for database viewer screen
 */
data class DatabaseViewerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val assignmentCount: Int = 0
)

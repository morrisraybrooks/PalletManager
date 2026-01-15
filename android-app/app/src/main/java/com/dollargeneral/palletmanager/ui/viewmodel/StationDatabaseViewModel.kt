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
class StationDatabaseViewModel @Inject constructor(
    private val repository: PalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationDatabaseUiState())
    val uiState: StateFlow<StationDatabaseUiState> = _uiState.asStateFlow()

    // All stations from the database - filtered by selected building
    val allStations: StateFlow<List<StationLookup>> = _uiState
        .flatMapLatest { state ->
            repository.getAllStations(state.selectedBuilding)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered stations based on search
    val filteredStations: StateFlow<List<StationLookup>> = combine(
        allStations,
        uiState.map { it.searchQuery }
    ) { stations, query ->
        if (query.isBlank()) {
            stations
        } else {
            stations.filter { station ->
                station.stationNumber.contains(query, ignoreCase = true) ||
                station.description.contains(query, ignoreCase = true) ||
                station.checkDigit.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "")
    }

    fun startAddingStation() {
        _uiState.value = _uiState.value.copy(
            newStationNumber = "",
            newCheckDigit = "",
            newDescription = "",
            editingStation = null,
            stationError = null,
            checkDigitError = null
        )
    }

    fun startEditingStation(station: StationLookup) {
        _uiState.value = _uiState.value.copy(
            newStationNumber = station.stationNumber,
            newCheckDigit = station.checkDigit,
            newDescription = station.description,
            editingStation = station,
            stationError = null,
            checkDigitError = null
        )
    }

    fun clearAddEditState() {
        _uiState.value = _uiState.value.copy(
            newStationNumber = "",
            newCheckDigit = "",
            newDescription = "",
            editingStation = null,
            stationError = null,
            checkDigitError = null,
            isSaving = false
        )
    }

    fun updateNewStationNumber(number: String) {
        _uiState.value = _uiState.value.copy(newStationNumber = number, stationError = null)
    }

    fun updateNewCheckDigit(digit: String) {
        _uiState.value = _uiState.value.copy(newCheckDigit = digit, checkDigitError = null)
    }

    fun updateNewDescription(description: String) {
        _uiState.value = _uiState.value.copy(newDescription = description)
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

    fun saveStation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val currentUiState = _uiState.value
            val stationNumber = currentUiState.newStationNumber
            val checkDigit = currentUiState.newCheckDigit
            val description = currentUiState.newDescription
            val editingStation = currentUiState.editingStation

            // Basic validation
            if (stationNumber.isBlank()) {
                _uiState.value = currentUiState.copy(stationError = "Station number cannot be empty", isSaving = false)
                return@launch
            }
            if (checkDigit.isBlank()) {
                _uiState.value = currentUiState.copy(checkDigitError = "Check digit cannot be empty", isSaving = false)
                return@launch
            }

            // Validate station number format using repository's validation logic
            val (isValidStation, normalizedStation) = repository.validateAndFormatStation(stationNumber)
            if (!isValidStation) {
                _uiState.value = currentUiState.copy(stationError = "Invalid station number format", isSaving = false)
                return@launch
            }

            // Validate check digit (simple numeric check for now)
            if (!checkDigit.matches(Regex("^\\d{2}$"))) {
                _uiState.value = currentUiState.copy(checkDigitError = "Check digit must be 2 digits", isSaving = false)
                return@launch
            }

            val result = repository.addOrUpdateStation(
                buildingNumber = currentUiState.selectedBuilding,
                stationNumber = normalizedStation,
                checkDigit = checkDigit,
                description = description
            )

            result
                .onSuccess {
                    _uiState.value = currentUiState.copy(
                        isSaving = false,
                        newStationNumber = "",
                        newCheckDigit = "",
                        newDescription = "",
                        editingStation = null,
                        stationError = null,
                        checkDigitError = null,
                        errorMessage = null
                    )
                    showMessage("Station saved successfully!")
                }
                .onFailure { e ->
                    _uiState.value = currentUiState.copy(
                        isSaving = false,
                        errorMessage = "Failed to save station: ${e.message}"
                    )
                }
        }
    }

    fun deleteStation(stationNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val buildingNumber = _uiState.value.selectedBuilding
            repository.deleteStation(buildingNumber, stationNumber)
                .onSuccess {
                    showMessage("Station deleted successfully!")
                }
                .onFailure { error ->
                    showError("Failed to delete station: ${error.message}")
                }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isLoading = false)
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isLoading = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun addBulkStation(stationNumber: String, checkDigit: String) {
        viewModelScope.launch {
            val buildingNumber = _uiState.value.selectedBuilding
            repository.addOrUpdateStation(buildingNumber, stationNumber, checkDigit)
                .onSuccess {
                    // Success handled silently for bulk operations
                }
                .onFailure { error ->
                    showError("Failed to add station $stationNumber: ${error.message}")
                }
        }
    }

    /**
     * Update selected building
     */
    fun updateSelectedBuilding(buildingNumber: Int) {
        _uiState.value = _uiState.value.copy(selectedBuilding = buildingNumber)
    }
}

/**
 * UI State for station database screen
 */
data class StationDatabaseUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val newStationNumber: String = "",
    val newCheckDigit: String = "",
    val newDescription: String = "",
    val editingStation: StationLookup? = null,
    val stationError: String? = null,
    val checkDigitError: String? = null,
    val isSaving: Boolean = false,
    val selectedBuilding: Int = 3 // Default to Building 3
)

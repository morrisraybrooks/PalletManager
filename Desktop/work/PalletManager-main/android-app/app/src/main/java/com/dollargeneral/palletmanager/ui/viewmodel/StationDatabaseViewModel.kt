package com.dollargeneral.palletmanager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollargeneral.palletmanager.data.entities.StationLookup
import com.dollargeneral.palletmanager.data.importer.StationDataImporter
import com.dollargeneral.palletmanager.data.repository.PalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StationDatabaseViewModel @Inject constructor(
    private val repository: PalletRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationDatabaseUiState())
    val uiState: StateFlow<StationDatabaseUiState> = _uiState.asStateFlow()

    // All stations from the database
    val allStations: StateFlow<List<StationLookup>> = repository.getAllStations()
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
            repository.deleteStation(stationNumber)
                .onSuccess {
                    showMessage("Station deleted successfully!")
                }
                .onFailure { error ->
                    showError("Failed to delete station: ${error.message}")
                }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isLoading = false)
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message, isLoading = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun addBulkStation(stationNumber: String, checkDigit: String) {
        viewModelScope.launch {
            repository.addOrUpdateStation(stationNumber, checkDigit)
                .onSuccess {
                    // Success handled silently for bulk operations
                }
                .onFailure { error ->
                    showError("Failed to add station $stationNumber: ${error.message}")
                }
        }
    }

    fun addBulkStations(stationData: List<Pair<String, String>>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBulkOperationInProgress = true,
                bulkOperationProgress = 0,
                bulkOperationTotal = stationData.size
            )

            var successCount = 0
            var failureCount = 0
            val failures = mutableListOf<String>()

            stationData.forEachIndexed { index, (stationNumber, checkDigit) ->
                repository.addOrUpdateStation(stationNumber, checkDigit)
                    .onSuccess {
                        successCount++
                    }
                    .onFailure { error ->
                        failureCount++
                        failures.add("$stationNumber: ${error.message}")
                    }

                // Update progress
                _uiState.value = _uiState.value.copy(
                    bulkOperationProgress = index + 1
                )
            }

            // Show completion message
            val message = when {
                failureCount == 0 -> "Successfully added $successCount stations"
                successCount == 0 -> "Failed to add all stations"
                else -> "Added $successCount stations, $failureCount failed"
            }

            _uiState.value = _uiState.value.copy(
                isBulkOperationInProgress = false,
                bulkOperationProgress = 0,
                bulkOperationTotal = 0,
                successMessage = message,
                errorMessage = if (failures.isNotEmpty()) failures.joinToString("\n") else null
            )

            // Clear success message after delay
            if (failureCount == 0) {
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(successMessage = null)
            }
        }
    }

    /**
     * Enhanced bulk add with real-time updates and immediate UI refresh
     * Provides better user feedback and ensures all screens update immediately
     */
    fun addBulkStationsWithRealTimeUpdates(stationData: List<Pair<String, String>>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBulkOperationInProgress = true,
                bulkOperationProgress = 0,
                bulkOperationTotal = stationData.size
            )

            var successCount = 0
            var failureCount = 0
            val failures = mutableListOf<String>()

            stationData.forEachIndexed { index, (stationNumber, checkDigit) ->
                val result = repository.addOrUpdateStation(stationNumber, checkDigit)
                result
                    .onSuccess {
                        successCount++
                    }
                    .onFailure { error ->
                        failureCount++
                        failures.add("$stationNumber: ${error.message}")
                    }
                // Update progress after each operation
                _uiState.value = _uiState.value.copy(
                    bulkOperationProgress = index + 1
                )
            }

            // Show completion message with detailed feedback
            val message = when {
                failureCount == 0 -> "✅ Successfully added $successCount stations to database!\n🔄 All screens will update automatically."
                successCount == 0 -> "❌ Failed to add all stations to database"
                else -> "⚠️ Added $successCount stations, $failureCount failed\n🔄 Successfully added stations are now available."
            }

            _uiState.value = _uiState.value.copy(
                isBulkOperationInProgress = false,
                bulkOperationProgress = 0,
                bulkOperationTotal = 0,
                successMessage = message,
                errorMessage = if (failures.isNotEmpty()) failures.joinToString("\n") else null
            )

            // Trigger explicit refresh to ensure UI updates immediately
            refreshData()

            // Clear success message after longer delay for better user feedback
            if (failureCount == 0) {
                kotlinx.coroutines.delay(5000)
                _uiState.value = _uiState.value.copy(successMessage = null)
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun importStationData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBulkOperationInProgress = true,
                bulkOperationProgress = 0,
                bulkOperationTotal = 0
            )

            val importer = StationDataImporter(repository, context.assets)
            importer.importYourRecordedData()
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        isBulkOperationInProgress = false,
                        successMessage = "Successfully imported $count stations from CSV"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isBulkOperationInProgress = false,
                        errorMessage = "Failed to import CSV data: ${error.message}"
                    )
                }
        }
    }
}

/**
 * UI State for station database screen
 */
data class StationDatabaseUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val newStationNumber: String = "",
    val newCheckDigit: String = "",
    val newDescription: String = "",
    val editingStation: StationLookup? = null,
    val stationError: String? = null,
    val checkDigitError: String? = null,
    val isSaving: Boolean = false,
    val isBulkOperationInProgress: Boolean = false,
    val bulkOperationProgress: Int = 0,
    val bulkOperationTotal: Int = 0
)

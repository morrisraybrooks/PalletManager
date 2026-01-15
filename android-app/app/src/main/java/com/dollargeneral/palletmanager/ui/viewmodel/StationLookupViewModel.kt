package com.dollargeneral.palletmanager.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollargeneral.palletmanager.data.database.StationUtils
import com.dollargeneral.palletmanager.data.database.ValidationResult
import com.dollargeneral.palletmanager.data.entities.StationLookup
import com.dollargeneral.palletmanager.data.repository.PalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * ViewModel following Android best practices
 * - Uses Hilt for dependency injection
 * - Repository pattern
 * - Optimized for warehouse efficiency and immediate feedback
 */
@HiltViewModel
class StationLookupViewModel @Inject constructor(
    private val repository: PalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationLookupUiState())
    val uiState: StateFlow<StationLookupUiState> = _uiState.asStateFlow()

    // Recent stations (last 10 used) - filtered by selected building
    val recentStations: StateFlow<List<StationLookup>> = _uiState
        .flatMapLatest { state ->
            repository.getAllStations(state.selectedBuilding)
        }
        .map { stations ->
            stations.filter { it.usageFrequency > 0 }
                .sortedByDescending { it.usageFrequency }
                .take(10)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Most frequently used stations - filtered by selected building
    val frequentStations: StateFlow<List<StationLookup>> = _uiState
        .flatMapLatest { state ->
            repository.getAllStations(state.selectedBuilding)
        }
        .map { stations ->
            stations.filter { it.usageFrequency >= 3 }
                .sortedByDescending { it.usageFrequency }
                .take(12)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load active assignment count
        viewModelScope.launch {
            repository.getActiveAssignmentCount().collect { count ->
                _uiState.value = _uiState.value.copy(activeAssignmentCount = count)
            }
        }
    }

    /**
     * Update station input and provide real-time validation
     */
    fun updateStationInput(input: String) {
        Log.d("StationLookupViewModel", "🔵 updateStationInput called with: '$input'")

        val cleanedInput = input.trim()
        Log.d("StationLookupViewModel", "🔵 Cleaned input: '$cleanedInput'")

        val validationResult = StationUtils.getValidationStatus(cleanedInput)
        Log.d("StationLookupViewModel", "🔵 Validation result: ${validationResult.name} (isValid=${validationResult.isValid})")

        val suggestions = StationUtils.getInputSuggestions(cleanedInput)
        val isComplete = isCompleteStationInput(cleanedInput)
        Log.d("StationLookupViewModel", "🔵 Is complete station input: $isComplete")

        _uiState.value = _uiState.value.copy(
            stationInput = cleanedInput,
            checkDigitResult = null,
            validationStatus = validationResult.message,
            inputSuggestions = suggestions
        )

        // Auto-lookup if input looks complete
        if (validationResult.isValid && isComplete) {
            Log.d("StationLookupViewModel", "✅ AUTO-TRIGGERING LOOKUP for '$cleanedInput'")
            lookupStation()
        } else {
            Log.d("StationLookupViewModel", "❌ NOT auto-triggering: isValid=${validationResult.isValid}, isComplete=$isComplete")
        }
    }

    /**
     * Perform station lookup
     */
    fun lookupStation() {
        val currentInput = _uiState.value.stationInput
        if (currentInput.isEmpty()) return

        Log.d("StationLookupViewModel", "🔍 lookupStation: '$currentInput'")
        Log.d("StationLookupViewModel", "🔍 Input length: ${currentInput.length}")
        Log.d("StationLookupViewModel", "🔍 Input bytes: ${currentInput.toByteArray().joinToString { it.toString() }}")

        _uiState.value = _uiState.value.copy(
            isLookingUp = true,
            checkDigitResult = null
        )

        viewModelScope.launch {
            try {
                val buildingNumber = _uiState.value.selectedBuilding
                Log.d("StationLookupViewModel", "🔍 Calling repository.lookupCheckDigit(building=$buildingNumber, station='$currentInput')")
                val checkDigit = repository.getCheckDigitForStation(buildingNumber, currentInput)
                Log.d("StationLookupViewModel", "🔍 Lookup result: '$checkDigit'")

                val normalizedStation = StationUtils.normalizeStationNumber(currentInput)
                Log.d("StationLookupViewModel", "🔍 Normalized station: '$normalizedStation'")

                _uiState.value = _uiState.value.copy(
                    isLookingUp = false,
                    checkDigitResult = checkDigit,
                    normalizedStation = normalizedStation,
                    lastLookupTime = Date()
                )

                // Record usage if check digit was found
                if (checkDigit != null) {
                    Log.d("StationLookupViewModel", "✅ Check digit found, recording usage")
                    repository.recordStationUsage(normalizedStation)
                } else {
                    Log.w("StationLookupViewModel", "❌ No check digit found for building $buildingNumber, station '$currentInput'")
                }
            } catch (e: Exception) {
                Log.e("StationLookupViewModel", "❌ Lookup failed for '$currentInput'", e)
                _uiState.value = _uiState.value.copy(
                    isLookingUp = false,
                    checkDigitResult = null
                )
            }
        }
    }

    /**
     * Select a station from quick access
     */
    fun selectStation(station: StationLookup) {
        Log.d("StationLookupViewModel", "selectStation: ${station.stationNumber}")
        
        val displayFormat = station.stationNumber
            .replace("03-", "3-")
            .replace("-01", "-1")
        
        _uiState.value = _uiState.value.copy(
            stationInput = displayFormat,
            checkDigitResult = station.checkDigit,
            normalizedStation = station.stationNumber,
            isLookingUp = false,
            validationStatus = "Station selected from quick access"
        )
    }

    /**
     * Clear all input and results
     */
    fun clearInput() {
        Log.d("StationLookupViewModel", "clearInput")
        _uiState.value = _uiState.value.copy(
            stationInput = "",
            checkDigitResult = null,
            normalizedStation = "",
            validationStatus = null,
            isLookingUp = false
        )
    }

    /**
     * Save a quick assignment with current station and check digit
     */
    fun saveQuickAssignment() {
        val currentState = _uiState.value
        val checkDigit = currentState.checkDigitResult
        val normalizedStation = currentState.normalizedStation
        
        if (checkDigit == null || normalizedStation.isEmpty()) {
            Log.w("StationLookupViewModel", "Cannot save - missing check digit or station")
            return
        }

        Log.d("StationLookupViewModel", "saveQuickAssignment: $normalizedStation -> $checkDigit")
        
        viewModelScope.launch {
            try {
                val result = repository.addAssignment(
                    productName = "", // Optional for quick assignments
                    destination = normalizedStation,
                    checkDigit = checkDigit,
                    notes = "Quick assignment from station lookup"
                )
                
                result.onSuccess {
                    Log.d("StationLookupViewModel", "Quick assignment saved successfully")
                    // Clear input after successful save
                    clearInput()
                }.onFailure { e ->
                    Log.e("StationLookupViewModel", "Failed to save quick assignment", e)
                }
            } catch (e: Exception) {
                Log.e("StationLookupViewModel", "Error saving quick assignment", e)
            }
        }
    }

    /**
     * Validate station input format and provide helpful feedback
     */
    private fun validateStationInput(input: String): String? {
        if (input.isEmpty()) return null
        
        return when {
            input.length < 3 -> "Keep typing..."
            isValidStationFormat(input) -> "Valid station format ✓"
            input.all { it.isDigit() } && input.length == 4 -> "Compact format detected"
            input.contains("-") -> "Station format detected"
            else -> "Try format: 3-40-15-1 or 4015"
        }
    }

    /**
     * Check if input looks like a complete station number
     */
    private fun isCompleteStationInput(input: String): Boolean {
        return when {
            // Compact format: 4015
            input.length == 4 && input.all { it.isDigit() } -> true
            // Standard format: 58-01 (our current database format)
            input.matches(Regex("\\d{2}-\\d{2}")) -> true
            // Full format: 3-40-15-1
            input.matches(Regex("\\d-\\d{2}-\\d{2}-\\d")) -> true
            // Partial format: 40-15
            input.matches(Regex("\\d{2}-\\d{2}")) -> true
            else -> false
        }
    }

    /**
     * Check if input matches expected station formats
     */
    private fun isValidStationFormat(input: String): Boolean {
        return StationUtils.isValidStationNumber(input) ||
               input.matches(Regex("\\d{4}")) || // 4015
               input.matches(Regex("\\d{2}-\\d{2}")) || // 40-15
               input.matches(Regex("\\d-\\d{2}-\\d{2}")) || // 3-40-15
               input.matches(Regex("\\d-\\d{2}-\\d{2}-\\d")) // 3-40-15-1
    }

    /**
     * Update selected building
     */
    fun updateSelectedBuilding(buildingNumber: Int) {
        Log.d("StationLookupViewModel", "updateSelectedBuilding: $buildingNumber")
        _uiState.value = _uiState.value.copy(
            selectedBuilding = buildingNumber,
            // Clear results when switching buildings
            checkDigitResult = null,
            normalizedStation = ""
        )
    }
}

/**
 * UI State for station lookup screen
 */
data class StationLookupUiState(
    val stationInput: String = "",
    val checkDigitResult: String? = null,
    val normalizedStation: String = "",
    val isLookingUp: Boolean = false,
    val validationStatus: String? = null,
    val inputSuggestions: List<String> = emptyList(),
    val activeAssignmentCount: Int = 0,
    val lastLookupTime: Date? = null,
    val selectedBuilding: Int = 3 // Default to Building 3
)

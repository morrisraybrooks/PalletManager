package com.dollargeneral.palletmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollargeneral.palletmanager.data.database.StationUtils
import com.dollargeneral.palletmanager.data.repository.PalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for adding new pallet assignments
 */
@HiltViewModel
class AddAssignmentViewModel @Inject constructor(
    private val repository: PalletRepository
) : ViewModel() {
    
    // Form state
    private val _uiState = MutableStateFlow(AddAssignmentUiState())
    val uiState: StateFlow<AddAssignmentUiState> = _uiState.asStateFlow()
    
    /**
     * Update product name
     */
    fun updateProductName(productName: String) {
        _uiState.value = _uiState.value.copy(
            productName = productName
            // No validation needed - product name is purely optional
        )
    }
    
    /**
     * Update destination and auto-lookup check digit
     */
    fun updateDestination(destination: String) {
        _uiState.value = _uiState.value.copy(
            destination = destination,
            destinationError = null,
            isLookingUpCheckDigit = true
        )

        viewModelScope.launch {
            if (destination.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    checkDigit = "",
                    suggestedCheckDigit = null,
                    isLookingUpCheckDigit = false,
                    normalizedDestination = ""
                )
                return@launch
            }

            try {
                val normalized = StationUtils.normalizeStationNumber(destination)
                val (isValid, _) = repository.validateAndFormatStation(normalized)

                if (isValid) {
                    val checkDigit = repository.getCheckDigitForStation(normalized)
                    _uiState.value = _uiState.value.copy(
                        checkDigit = checkDigit ?: "",
                        suggestedCheckDigit = checkDigit,
                        isLookingUpCheckDigit = false,
                        normalizedDestination = normalized,
                        destinationError = if (checkDigit == null) "Check digit not found for $normalized" else null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLookingUpCheckDigit = false,
                        suggestedCheckDigit = null,
                        normalizedDestination = if (destination.isNotEmpty()) normalized else "",
                        destinationError = if (destination.isNotEmpty()) "Invalid station format" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLookingUpCheckDigit = false,
                    destinationError = "Error looking up check digit: ${e.message}",
                    normalizedDestination = ""
                )
            }
        }
    }
    
    /**
     * Update check digit
     */
    fun updateCheckDigit(checkDigit: String) {
        _uiState.value = _uiState.value.copy(
            checkDigit = checkDigit,
            checkDigitError = null
        )
    }
    
    /**
     * Update notes
     */
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    /**
     * Use the suggested check digit
     */
    fun useSuggestedCheckDigit() {
        _uiState.value.suggestedCheckDigit?.let { suggested ->
            _uiState.value = _uiState.value.copy(
                checkDigit = suggested,
                checkDigitError = null
            )
        }
    }
    
    /**
     * Validate and save the assignment
     */
    fun saveAssignment(): Boolean {
        val state = _uiState.value
        val errors = mutableMapOf<String, String>()
        
        // Validate destination
        if (state.destination.isBlank()) {
            errors["destination"] = "Destination is required"
        } else {
            val (isValid, _) = repository.validateAndFormatStation(state.destination)
            if (!isValid) {
                errors["destination"] = "Invalid destination format (use X-XX-XX-X)"
            }
        }
        
        // Validate check digit
        if (state.checkDigit.isBlank()) {
            errors["checkDigit"] = "Check digit is required"
        } else if (!state.checkDigit.matches(Regex("\\d{1,3}"))) {
            errors["checkDigit"] = "Check digit must be a number"
        }
        
        // Update UI with errors
        _uiState.value = _uiState.value.copy(
            destinationError = errors["destination"],
            checkDigitError = errors["checkDigit"]
        )
        
        // If no errors, save the assignment
        if (errors.isEmpty()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSaving = true)
                
                repository.addAssignment(
                    productName = state.productName,
                    destination = state.destination,
                    checkDigit = state.checkDigit,
                    notes = state.notes
                ).onSuccess {
                    // Also save the check digit to the database if it's new
                    if (state.suggestedCheckDigit == null) {
                        repository.addOrUpdateStation(
                            stationNumber = state.destination,
                            checkDigit = state.checkDigit,
                            description = if (state.productName.isNotEmpty()) 
                                "Added from ${state.productName}" else ""
                        )
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSaved = true,
                        message = "Assignment added successfully!"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        message = "Failed to save: ${error.message}",
                        isError = true
                    )
                }
            }
            return true
        }
        
        return false
    }
    
    /**
     * Reset the form
     */
    fun resetForm() {
        _uiState.value = AddAssignmentUiState()
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
}

/**
 * UI State for adding assignments
 */
data class AddAssignmentUiState(
    val productName: String = "",
    val destination: String = "",
    val checkDigit: String = "",
    val notes: String = "",
    
    // Validation errors (product name doesn't need validation)
    val destinationError: String? = null,
    val checkDigitError: String? = null,
    
    // Auto-lookup state
    val isLookingUpCheckDigit: Boolean = false,
    val suggestedCheckDigit: String? = null,
    val normalizedDestination: String = "",
    
    // Save state
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

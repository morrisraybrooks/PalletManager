package com.dollargeneral.palletmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dollargeneral.palletmanager.ui.viewmodel.AddAssignmentViewModel

/**
 * Screen for adding new pallet assignments
 * Optimized for quick entry with auto-lookup functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentScreen(
    onNavigateBack: () -> Unit,
    onAssignmentSaved: () -> Unit,
    viewModel: AddAssignmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onAssignmentSaved()
        }
    }

    uiState.message?.let {
        LaunchedEffect(it) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pallet Assignment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Building Selector
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Building Selection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    com.dollargeneral.palletmanager.ui.components.BuildingSelector(
                        selectedBuilding = uiState.selectedBuilding,
                        onBuildingSelected = viewModel::updateSelectedBuilding,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Destination and Check Digit
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.destination,
                        onValueChange = viewModel::updateDestination,
                        label = { Text("Destination *") },
                        placeholder = { Text("e.g., 3-58-15") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        isError = uiState.destinationError != null
                    )
                    uiState.destinationError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.checkDigit,
                        onValueChange = viewModel::updateCheckDigit,
                        label = { Text("Check Digit *") },
                        placeholder = { Text("Auto-filled or enter manually") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                        isError = uiState.checkDigitError != null
                    )
                    uiState.checkDigitError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }

                    uiState.suggestedCheckDigit?.let { suggested ->
                        if (suggested != uiState.checkDigit) {
                            SuggestionChip(
                                onClick = viewModel::useSuggestedCheckDigit,
                                label = { Text("Suggested: $suggested") },
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Optional Information
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = uiState.productName,
                        onValueChange = viewModel::updateProductName,
                        label = { Text("Product Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::updateNotes,
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        maxLines = 3
                    )
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::resetForm,
                    modifier = Modifier.weight(1f).height(56.dp),
                    enabled = !uiState.isSaving
                ) {
                    Text("Clear", fontSize = 16.sp)
                }
                Button(
                    onClick = { viewModel.saveAssignment() },
                    modifier = Modifier.weight(2f).height(56.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save Assignment", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

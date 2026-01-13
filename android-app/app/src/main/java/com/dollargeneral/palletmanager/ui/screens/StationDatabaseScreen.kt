package com.dollargeneral.palletmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dollargeneral.palletmanager.data.entities.StationLookup
import com.dollargeneral.palletmanager.ui.viewmodel.StationDatabaseViewModel

/**
 * Screen for managing station check digit database
 * Allows viewing, adding, editing, and deleting station check digits
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDatabaseScreen(
    onNavigateBack: () -> Unit,
    viewModel: StationDatabaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredStations by viewModel.filteredStations.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var showBulkAddDialog by remember { mutableStateOf(false) }

    uiState.errorMessage?.let {
        LaunchedEffect(it) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Station Database", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = {
                        viewModel.startAddingStation()
                        showAddEditDialog = true
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Station")
                }

                FloatingActionButton(
                    onClick = { showBulkAddDialog = true },
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = "Bulk Add Aisle")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Search Stations") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Stations: ${filteredStations.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = { /* TODO: Import station data */ },
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Data")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (filteredStations.isEmpty()) {
                Text("No stations found.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredStations) { station ->
                        StationCard(
                            station = station,
                            onEdit = { editStation ->
                                viewModel.startEditingStation(editStation)
                                showAddEditDialog = true
                            },
                            onDelete = { deleteStation -> viewModel.deleteStation(deleteStation.stationNumber) }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditStationDialog(
            uiState = uiState,
            onDismiss = {
                showAddEditDialog = false
                viewModel.clearAddEditState()
            },
            onStationNumberChange = viewModel::updateNewStationNumber,
            onCheckDigitChange = viewModel::updateNewCheckDigit,
            onDescriptionChange = viewModel::updateNewDescription,
            onSave = {
                viewModel.saveStation()
                showAddEditDialog = false
            }
        )
    }

    if (showBulkAddDialog) {
        BulkAddAisleDialog(
            onDismiss = { showBulkAddDialog = false },
            onAddAisle = { aisleNumber, checkDigits ->
                // Add all stations for this aisle
                checkDigits.forEachIndexed { index, checkDigit ->
                    if (checkDigit.isNotBlank()) {
                        val stationNumber = "03-${aisleNumber.padStart(2, '0')}-${(index + 1).toString().padStart(2, '0')}-01"
                        viewModel.addBulkStation(stationNumber, checkDigit)
                    }
                }
                showBulkAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyStationsContent(
    hasSearch: Boolean,
    onAddStation: () -> Unit,
    onImportSample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            if (hasSearch) Icons.Default.SearchOff else Icons.Default.Storage,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasSearch) "No Stations Found" else "No Stations Yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasSearch)
                "Try a different search term"
            else
                "Add your recorded check digits to get started",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!hasSearch) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onImportSample,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Sample", fontSize = 14.sp)
                }

                Button(
                    onClick = onAddStation,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Station", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StationCard(
    station: StationLookup,
    onEdit: (StationLookup) -> Unit,
    onDelete: (StationLookup) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.stationNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (station.description.isNotEmpty()) {
                    Text(
                        text = station.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (station.usageFrequency > 0) {
                    Text(
                        text = "Used ${station.usageFrequency} times",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Check digit
            Text(
                text = station.checkDigit,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            // Action buttons
            Row {
                IconButton(onClick = { onEdit(station) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { onDelete(station) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEditStationDialog(
    uiState: com.dollargeneral.palletmanager.ui.viewmodel.StationDatabaseUiState,
    onDismiss: () -> Unit,
    onStationNumberChange: (String) -> Unit,
    onCheckDigitChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = if (uiState.editingStation != null) "Edit Station" else "Add Station",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Station Number Field
                OutlinedTextField(
                    value = uiState.newStationNumber,
                    onValueChange = onStationNumberChange,
                    label = { Text("Station Number *") },
                    placeholder = { Text("e.g., 3-58-15-1") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = uiState.stationError != null,
                    enabled = uiState.editingStation == null // Don't allow editing station number
                )

                uiState.stationError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                // Check Digit Field
                OutlinedTextField(
                    value = uiState.newCheckDigit,
                    onValueChange = onCheckDigitChange,
                    label = { Text("Check Digit *") },
                    placeholder = { Text("e.g., 21, 99") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = uiState.checkDigitError != null
                )

                uiState.checkDigitError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                // Description Field
                OutlinedTextField(
                    value = uiState.newDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g., Dog Food Section") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        Text("Cancel", fontSize = 16.sp)
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (uiState.editingStation != null) "Update" else "Save",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
@Composable
private fun AddEditStationDialog(
    uiState: com.dollargeneral.palletmanager.ui.viewmodel.StationDatabaseUiState,
    onDismiss: () -> Unit,
    onStationNumberChange: (String) -> Unit,
    onCheckDigitChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = if (uiState.editingStation != null) "Edit Station" else "Add Station",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // Station Number Field
                OutlinedTextField(
                    value = uiState.newStationNumber,
                    onValueChange = onStationNumberChange,
                    label = { Text("Station Number *") },
                    placeholder = { Text("e.g., 3-58-15-1") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = uiState.stationError != null,
                    enabled = uiState.editingStation == null // Don't allow editing station number
                )

                uiState.stationError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                // Check Digit Field
                OutlinedTextField(
                    value = uiState.newCheckDigit,
                    onValueChange = onCheckDigitChange,
                    label = { Text("Check Digit *") },
                    placeholder = { Text("e.g., 21, 99") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    isError = uiState.checkDigitError != null
                )

                uiState.checkDigitError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                // Description Field
                OutlinedTextField(
                    value = uiState.newDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g., Dog Food Section") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (uiState.editingStation != null) "Update" else "Save")
                        }
                    }
                }
            }
        }
    }
}
*/

@Composable
private fun BulkAddAisleDialog(
    onDismiss: () -> Unit,
    onAddAisle: (aisleNumber: String, checkDigits: List<String>) -> Unit
) {
    var aisleNumber by remember { mutableStateOf("") }
    var checkDigitsText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Bulk Add Aisle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Add all check digits for an entire aisle at once",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = aisleNumber,
                    onValueChange = { aisleNumber = it },
                    label = { Text("Aisle Number") },
                    placeholder = { Text("e.g., 57, 58, 59") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = checkDigitsText,
                    onValueChange = { checkDigitsText = it },
                    label = { Text("Check Digits (one per line)") },
                    placeholder = { Text("23\n59\n82\n58\n...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 20,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text(
                    text = "Enter one check digit per line for positions 01, 02, 03, etc. Leave blank lines for missing positions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val checkDigits = checkDigitsText.split("\n").map { it.trim() }
                            onAddAisle(aisleNumber, checkDigits)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = aisleNumber.isNotBlank() && checkDigitsText.isNotBlank()
                    ) {
                        Text("Add Aisle")
                    }
                }
            }
        }
    }
}

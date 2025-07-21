package com.dollargeneral.palletmanager.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
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
    onNavigateToDbViewer: () -> Unit = {},
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

    uiState.successMessage?.let {
        LaunchedEffect(it) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSuccessMessage()
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
                },
                actions = {
                    IconButton(onClick = onNavigateToDbViewer) {
                        Icon(Icons.Default.TableView, contentDescription = "View Database")
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
                    onClick = { viewModel.importStationData() },
                    enabled = !uiState.isLoading && !uiState.isBulkOperationInProgress
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

            // Bulk operation progress
            if (uiState.isBulkOperationInProgress) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Adding stations...",
                            style = MaterialTheme.typography.titleMedium
                        )

                        LinearProgressIndicator(
                            progress = if (uiState.bulkOperationTotal > 0) {
                                uiState.bulkOperationProgress.toFloat() / uiState.bulkOperationTotal.toFloat()
                            } else 0f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "${uiState.bulkOperationProgress} of ${uiState.bulkOperationTotal} stations",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Success message
            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

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

    // Success/Error message display
    uiState.successMessage?.let { message ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.clearSuccessMessage() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.clearErrorMessage() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
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
            onAddAisle = { aisleNumber, startSection, endSection, checkDigits ->
                try {
                    // Prepare station data for bulk operation with proper error handling
                    val start = startSection.toIntOrNull() ?: 1
                    val stationData = checkDigits.mapIndexedNotNull { index, checkDigit ->
                        // Skip blank lines (missing stations) but include "00" as valid check digit
                        if (checkDigit.isNotBlank()) {
                            val sectionNumber = start + index
                            val stationNumber = "03-${aisleNumber.padStart(2, '0')}-${sectionNumber.toString().padStart(2, '0')}-01"
                            stationNumber to checkDigit
                        } else null
                    }

                    // Use the enhanced bulk operation method with real-time updates
                    viewModel.addBulkStationsWithRealTimeUpdates(stationData)
                    showBulkAddDialog = false
                } catch (e: Exception) {
                    // Handle any unexpected errors gracefully
                    viewModel.showError("Error processing bulk add: ${e.message}")
                    showBulkAddDialog = false
                }
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
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Sample")
                }

                Button(
                    onClick = onAddStation,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Station")
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
    onAddAisle: (aisleNumber: String, startSection: String, endSection: String, checkDigits: List<String>) -> Unit
) {
    var aisleNumber by remember { mutableStateOf("") }
    var startSection by remember { mutableStateOf("01") }
    var endSection by remember { mutableStateOf("63") }
    var checkDigitsText by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }
    var useImprovedUI by remember { mutableStateOf(true) }

    // Calculate station numbers that will be created
    val stationNumbers = remember(aisleNumber, startSection, endSection) {
        if (aisleNumber.isNotBlank() && startSection.isNotBlank() && endSection.isNotBlank()) {
            try {
                val start = startSection.toInt()
                val end = endSection.toInt()
                if (start <= end && start > 0 && end <= 99) {
                    (start..end).map { section ->
                        "03-${aisleNumber.padStart(2, '0')}-${section.toString().padStart(2, '0')}-01"
                    }
                } else emptyList()
            } catch (e: NumberFormatException) {
                emptyList()
            }
        } else emptyList()
    }

    // Parse check digits
    val checkDigits = remember(checkDigitsText) {
        checkDigitsText.split("\n").map { it.trim() }
    }

    // Validation
    val validationError = remember(stationNumbers, checkDigits) {
        when {
            stationNumbers.isEmpty() -> "Please enter valid aisle and section numbers"
            checkDigits.size != stationNumbers.size ->
                "Number of check digits (${checkDigits.size}) must match number of stations (${stationNumbers.size})"
            else -> null
        }
    }

    // Check if all required check digits are entered (for save button state)
    val allCheckDigitsEntered = remember(stationNumbers, checkDigits) {
        stationNumbers.isNotEmpty() &&
        checkDigits.size == stationNumbers.size &&
        checkDigits.all { it.isNotBlank() && it.matches(Regex("^\\d{2}$")) }
    }

    // Count of completed check digits for progress display
    val completedCount = remember(checkDigits) {
        checkDigits.count { it.isNotBlank() && it.matches(Regex("^\\d{2}$")) }
    }

    // Responsive dialog that adapts to screen size
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // Make the entire content scrollable for phone compatibility
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bulk Add Aisle",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Add check digits for a range of stations in an aisle",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Input fields section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = aisleNumber,
                        onValueChange = { aisleNumber = it },
                        label = { Text("Aisle") },
                        placeholder = { Text("57") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = startSection,
                        onValueChange = { startSection = it },
                        label = { Text("Start") },
                        placeholder = { Text("01") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = endSection,
                        onValueChange = { endSection = it },
                        label = { Text("End") },
                        placeholder = { Text("63") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // UI Mode Toggle section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (useImprovedUI) "✨ Enhanced UI (Better Visual Association)" else "📝 Classic UI (Separate Columns)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Switch(
                        checked = useImprovedUI,
                        onCheckedChange = { useImprovedUI = it }
                    )
                }

                // Main content section - responsive height
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Re-adding a fixed height for the content area
                ) {
                    if (useImprovedUI) {
                        // IMPROVED UI: Paired Station-CheckDigit Input
                        ImprovedStationCheckDigitInput(
                            stationNumbers = stationNumbers,
                            checkDigitsText = checkDigitsText,
                            onCheckDigitsChange = { checkDigitsText = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // ORIGINAL UI: Separate columns
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Station numbers preview
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Station Numbers (${stationNumbers.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Column(
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    stationNumbers.forEachIndexed { index, stationNumber ->
                                        Text(
                                            text = "${index + 1}. $stationNumber",
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            // Check digits input
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Check Digits (${checkDigits.size})",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                OutlinedTextField(
                                    value = checkDigitsText,
                                    onValueChange = { checkDigitsText = it },
                                    label = { Text("One per line") },
                                    placeholder = { Text("23\n59\n\n82\n58\n...") },
                                    modifier = Modifier,
                                    maxLines = Int.MAX_VALUE,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }

                // Help text
                Text(
                    text = "💡 Enter one check digit per line. Leave blank lines for missing stations (breaseways). '00' is a valid check digit.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Validation error section
                validationError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }

                // Progress indicator section
                if (stationNumbers.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress: $completedCount / ${stationNumbers.size} stations",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            LinearProgressIndicator(
                                progress = if (stationNumbers.isEmpty()) 0f else completedCount.toFloat() / stationNumbers.size,
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }
                }

                // Action buttons section
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
                            if (allCheckDigitsEntered) {
                                onAddAisle(aisleNumber, startSection, endSection, checkDigits)
                            }
                        },
                        enabled = allCheckDigitsEntered,
                        modifier = Modifier.weight(2f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Save Aisle to Database")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImprovedStationCheckDigitInput(
    stationNumbers: List<String>,
    checkDigitsText: String,
    onCheckDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val checkDigits = checkDigitsText.split("\n").map { it.trim() }

    Column(modifier = modifier) {
        Text(
            text = "🎯 Station → Check Digit Pairs (${stationNumbers.size} stations)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            itemsIndexed(stationNumbers) { index, stationNumber ->
                StationCheckDigitRow(
                    index = index,
                    stationNumber = stationNumber,
                    checkDigit = checkDigits.getOrElse(index) { "" },
                    onCheckDigitChange = { newCheckDigit ->
                        val updatedCheckDigits = checkDigits.toMutableList()
                        // Ensure the list is large enough
                        while (updatedCheckDigits.size <= index) {
                            updatedCheckDigits.add("")
                        }
                        updatedCheckDigits[index] = newCheckDigit
                        onCheckDigitsChange(updatedCheckDigits.joinToString("\n"))
                    }
                )
            }
        }
    }
}

@Composable
private fun StationCheckDigitRow(
    index: Int,
    stationNumber: String,
    checkDigit: String,
    onCheckDigitChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checkDigit.isNotBlank())
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Station number with visual emphasis
            Column(modifier = Modifier.weight(2f)) {
                Text(
                    text = "${index + 1}.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stationNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Visual arrow connector
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "maps to",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Check digit input with immediate visual feedback
            OutlinedTextField(
                value = checkDigit,
                onValueChange = onCheckDigitChange,
                label = { Text("Check Digit", fontSize = 10.sp) },
                placeholder = { Text("00", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )

            // Status indicator
            Icon(
                if (checkDigit.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (checkDigit.isNotBlank()) "Complete" else "Pending",
                modifier = Modifier.size(20.dp),
                tint = if (checkDigit.isNotBlank())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun BulkAddConfirmationDialog(
    stationNumbers: List<String>,
    checkDigits: List<String>,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Confirm Bulk Add",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Review the stations and check digits that will be added:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${stationNumbers.size} stations will be added",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Station and check digit preview
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(stationNumbers) { index, stationNumber ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. $stationNumber",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "→ ${checkDigits.getOrElse(index) { "??" }}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add All Stations")
                    }
                }
            }
        }
    }
}
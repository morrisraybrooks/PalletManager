package com.dollargeneral.palletmanager.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dollargeneral.palletmanager.data.entities.StationLookup
import com.dollargeneral.palletmanager.ui.viewmodel.StationLookupViewModel

/**
 * Main station lookup screen - optimized for warehouse efficiency
 * Features large touch targets, immediate feedback, and minimal cognitive load
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationLookupScreen(
    onNavigateToAssignments: () -> Unit,
    onNavigateToDatabase: () -> Unit,
    viewModel: StationLookupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentStations by viewModel.recentStations.collectAsStateWithLifecycle()
    val frequentStations by viewModel.frequentStations.collectAsStateWithLifecycle()
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Auto-focus on station input when screen loads
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with branding and quick actions
        StationLookupHeader(
            onNavigateToAssignments = onNavigateToAssignments,
            onNavigateToDatabase = onNavigateToDatabase,
            assignmentCount = uiState.activeAssignmentCount
        )
        
        // Main station input area - takes center stage
        StationInputSection(
            stationInput = uiState.stationInput,
            onStationInputChange = viewModel::updateStationInput,
            checkDigitResult = uiState.checkDigitResult,
            isLookingUp = uiState.isLookingUp,
            validationStatus = uiState.validationStatus,
            focusRequester = focusRequester,
            onLookupStation = {
                viewModel.lookupStation()
                keyboardController?.hide()
            },
            onClearInput = viewModel::clearInput,
            onSaveAssignment = viewModel::saveQuickAssignment
        )
        
        // Quick access to recent and frequent stations
        QuickAccessSection(
            recentStations = recentStations,
            frequentStations = frequentStations,
            onStationSelected = { station ->
                viewModel.selectStation(station)
                keyboardController?.hide()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationLookupHeader(
    onNavigateToAssignments: () -> Unit,
    onNavigateToDatabase: () -> Unit,
    assignmentCount: Int
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Station Lookup",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Dollar General - Building 3",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Active assignments indicator
            if (assignmentCount > 0) {
                Button(
                    onClick = onNavigateToAssignments,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = "View Assignments",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$assignmentCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Database management
            IconButton(
                onClick = onNavigateToDatabase,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = "Station Database",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun StationInputSection(
    stationInput: String,
    onStationInputChange: (String) -> Unit,
    checkDigitResult: String?,
    isLookingUp: Boolean,
    validationStatus: String?,
    focusRequester: FocusRequester,
    onLookupStation: () -> Unit,
    onClearInput: () -> Unit,
    onSaveAssignment: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Station input field - extra large for glove use
            OutlinedTextField(
                value = stationInput,
                onValueChange = onStationInputChange,
                label = { 
                    Text(
                        "Enter Station Number",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                placeholder = { 
                    Text(
                        "e.g., 3-40-15-1 or 4015",
                        fontSize = 18.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .focusRequester(focusRequester),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onLookupStation() }
                ),
                leadingIcon = {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = if (stationInput.isNotEmpty()) {
                    {
                        IconButton(onClick = onClearInput) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Validation status
            validationStatus?.let { status ->
                Text(
                    text = status,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Check digit result display - highly prominent
            CheckDigitDisplay(
                checkDigit = checkDigitResult,
                isLookingUp = isLookingUp,
                hasInput = stationInput.isNotEmpty()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lookup button
                Button(
                    onClick = onLookupStation,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    enabled = stationInput.isNotEmpty() && !isLookingUp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLookingUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Lookup",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Quick save button (if check digit found)
                if (checkDigitResult != null) {
                    Button(
                        onClick = onSaveAssignment,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Save",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckDigitDisplay(
    checkDigit: String?,
    isLookingUp: Boolean,
    hasInput: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    checkDigit != null -> Color(0xFF4CAF50).copy(alpha = 0.1f) // Success green
                    hasInput && !isLookingUp -> Color(0xFFF44336).copy(alpha = 0.1f) // Error red
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .border(
                width = 3.dp,
                color = when {
                    checkDigit != null -> Color(0xFF4CAF50) // Success green
                    hasInput && !isLookingUp -> Color(0xFFF44336) // Error red
                    else -> MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLookingUp -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Looking up...",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            checkDigit != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Found",
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Check Digit:",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        checkDigit,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                }
            }
            hasInput -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Not Found",
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Station Not Found",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF44336),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Check station number format",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Ready",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Enter Station Number",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Check digit will appear here",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAccessSection(
    recentStations: List<StationLookup>,
    frequentStations: List<StationLookup>,
    onStationSelected: (StationLookup) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Recent stations
        if (recentStations.isNotEmpty()) {
            item {
                QuickAccessGroup(
                    title = "Recently Used",
                    icon = Icons.Default.History,
                    stations = recentStations.take(5),
                    onStationSelected = onStationSelected
                )
            }
        }

        // Frequent stations
        if (frequentStations.isNotEmpty()) {
            item {
                QuickAccessGroup(
                    title = "Most Used",
                    icon = Icons.Default.Star,
                    stations = frequentStations.take(8),
                    onStationSelected = onStationSelected
                )
            }
        }

        // Add bottom padding for last item
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickAccessGroup(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    stations: List<StationLookup>,
    onStationSelected: (StationLookup) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(stations) { station ->
                    StationQuickAccessCard(
                        station = station,
                        onClick = { onStationSelected(station) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StationQuickAccessCard(
    station: StationLookup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = station.stationNumber.replace("03-", "").replace("-01", ""),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CD: ${station.checkDigit}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            if (station.usageFrequency > 0) {
                Text(
                    text = "Used ${station.usageFrequency}x",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

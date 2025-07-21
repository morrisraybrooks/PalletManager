package com.dollargeneral.palletmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dollargeneral.palletmanager.data.entities.StationLookup
import com.dollargeneral.palletmanager.ui.viewmodel.DatabaseViewerViewModel

/**
 * Screen for viewing database contents
 * Shows all tables and their data in a tabular format
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseViewerScreen(
    onNavigateBack: () -> Unit,
    viewModel: DatabaseViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    
    var selectedTable by remember { mutableStateOf("Stations") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Viewer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
        ) {
            // Table selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTable == "Stations",
                    onClick = { selectedTable = "Stations" },
                    label = { Text("Stations") }
                )
                
                FilterChip(
                    selected = selectedTable == "Assignments",
                    onClick = { selectedTable = "Assignments" },
                    label = { Text("Assignments") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Database stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${stations.size}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Stations",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${uiState.assignmentCount}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Assignments",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Table header
            when (selectedTable) {
                "Stations" -> {
                    StationsTable(stations = stations, isLoading = uiState.isLoading)
                }
                "Assignments" -> {
                    Text("Assignments table view coming soon")
                }
            }
        }
    }
}

@Composable
fun StationsTable(
    stations: List<StationLookup>,
    isLoading: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Table header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Station Number",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.5f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Check Digit",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Description",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(2f),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Usage",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.5f),
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Table content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (stations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No stations found in database")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(stations) { station ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = station.stationNumber,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            text = station.checkDigit,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = station.description.ifEmpty { "-" },
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "${station.usageFrequency}",
                            modifier = Modifier.weight(0.5f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

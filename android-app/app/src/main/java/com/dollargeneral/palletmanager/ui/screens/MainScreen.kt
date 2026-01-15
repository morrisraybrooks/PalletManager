package com.dollargeneral.palletmanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
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
import com.dollargeneral.palletmanager.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * SIMPLIFIED Station-Centric Main Screen for testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToAddAssignment: () -> Unit,
    onNavigateToStationDatabase: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var stationInput by remember { mutableStateOf("") }
    var checkDigitResult by remember { mutableStateOf<String?>(null) }
    var isLookingUp by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
    ) {
        // Header - Station Lookup focused
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "🎯 STATION LOOKUP",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "NEW STATION-CENTRIC INTERFACE",
                        fontSize = 16.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            actions = {
                // Database management
                IconButton(
                    onClick = onNavigateToStationDatabase,
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
                containerColor = Color.Yellow.copy(alpha = 0.3f)
            )
        )
        
        // Main station input area - LARGE and prominent
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Blue.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚀 NEW STATION-CENTRIC UI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Station input field - EXTRA LARGE for glove use
                OutlinedTextField(
                    value = stationInput,
                    onValueChange = { 
                        stationInput = it
                        checkDigitResult = null
                    },
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
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            isLookingUp = true
                            coroutineScope.launch {
                                checkDigitResult = viewModel.lookupStation(uiState.selectedBuilding, stationInput)
                                isLookingUp = false
                            }
                            keyboardController?.hide()
                        }
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
                            IconButton(onClick = { 
                                stationInput = ""
                                checkDigitResult = null
                            }) {
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
                
                // CHECK DIGIT DISPLAY - HUGE and prominent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            when {
                                checkDigitResult != null -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                stationInput.isNotEmpty() && !isLookingUp -> Color(0xFFF44336).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                        .border(
                            width = 3.dp,
                            color = when {
                                checkDigitResult != null -> Color(0xFF4CAF50)
                                stationInput.isNotEmpty() && !isLookingUp -> Color(0xFFF44336)
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
                        checkDigitResult != null -> {
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
                                    checkDigitResult!!,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        stationInput.isNotEmpty() -> {
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons - LARGE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            isLookingUp = true
                            coroutineScope.launch {
                                checkDigitResult = viewModel.lookupStation(uiState.selectedBuilding, stationInput)
                                isLookingUp = false
                            }
                            keyboardController?.hide()
                        },
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
                    
                    if (checkDigitResult != null) {
                        Button(
                            onClick = { 
                                // Quick save simulation
                                stationInput = ""
                                checkDigitResult = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
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
}

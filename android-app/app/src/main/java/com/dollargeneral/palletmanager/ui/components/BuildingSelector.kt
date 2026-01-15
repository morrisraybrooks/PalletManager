package com.dollargeneral.palletmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Building selector dropdown component
 * Allows users to select which building's station data to view/edit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingSelector(
    selectedBuilding: Int,
    onBuildingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    availableBuildings: List<Int> = listOf(2, 3, 4) // Default buildings
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = "Building $selectedBuilding",
            onValueChange = {},
            readOnly = true,
            label = { Text("Building") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select building"
                )
            },
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableBuildings.forEach { building ->
                DropdownMenuItem(
                    text = { Text("Building $building") },
                    onClick = {
                        onBuildingSelected(building)
                        expanded = false
                    },
                    leadingIcon = if (building == selectedBuilding) {
                        { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

/**
 * Compact building selector for toolbar/header
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactBuildingSelector(
    selectedBuilding: Int,
    onBuildingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    availableBuildings: List<Int> = listOf(2, 3, 4)
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Building:",
            style = MaterialTheme.typography.labelMedium
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            FilledTonalButton(
                onClick = { expanded = true },
                modifier = Modifier.menuAnchor()
            ) {
                Text("$selectedBuilding")
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select building",
                    modifier = Modifier.size(20.dp)
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableBuildings.forEach { building ->
                    DropdownMenuItem(
                        text = { Text("Building $building") },
                        onClick = {
                            onBuildingSelected(building)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


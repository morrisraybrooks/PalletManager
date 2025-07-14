package com.dollargeneral.palletmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.dollargeneral.palletmanager.navigation.PalletManagerNavigation
import com.dollargeneral.palletmanager.ui.theme.PalletManagerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity following Android best practices
 * - Uses Hilt for dependency injection
 * - Optimized for tablet use in warehouse environment
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PalletManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PalletManagerNavigation()
                }
            }
        }
    }
}

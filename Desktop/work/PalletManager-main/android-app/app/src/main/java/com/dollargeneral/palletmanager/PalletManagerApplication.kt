package com.dollargeneral.palletmanager

import android.app.Application
import android.util.Log
import com.dollargeneral.palletmanager.data.importer.StationDataImporter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class following Android best practices
 * - Uses Hilt for dependency injection
 * - Proper coroutine scope management
 * - Separation of concerns
 */
@HiltAndroidApp
class PalletManagerApplication : Application() {

    @Inject
    lateinit var stationDataImporter: StationDataImporter

    // Application-scoped coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        Log.d("PalletManagerApp", "🚀 APPLICATION STARTING")
        super.onCreate()

        // Initialize station data import in background
        applicationScope.launch {
            try {
                Log.d("PalletManagerApp", "📊 Starting background station import")
                val result = stationDataImporter.importYourRecordedData()
                result.onSuccess { count ->
                    Log.d("PalletManagerApp", "✅ Successfully imported $count stations")
                }.onFailure { error ->
                    Log.e("PalletManagerApp", "❌ Import failed: ${error.message}", error)
                }
            } catch (e: Exception) {
                Log.e("PalletManagerApp", "❌ Application import error: ${e.message}", e)
            }
        }

        Log.d("PalletManagerApp", "🎯 APPLICATION READY")
    }
}

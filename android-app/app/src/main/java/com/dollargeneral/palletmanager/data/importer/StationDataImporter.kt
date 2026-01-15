package com.dollargeneral.palletmanager.data.importer

import android.content.res.AssetManager
import android.util.Log
import com.dollargeneral.palletmanager.data.repository.PalletRepository
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for importing station check digit data
 * Helps load your recorded check digits from notebook into the app
 */
class StationDataImporter(
    private val repository: PalletRepository,
    private val assetManager: AssetManager
) {

    /**
     * Import ALL your recorded check digits from station_data.csv
     * This includes thousands of actual check digits you've recorded
     */
    suspend fun importYourRecordedData(): Result<Int> {
        Log.d("StationDataImporter", "🚀 Starting station data import")

        try {
            // Force fresh import for simplified format (database version 2)
            val existingCount = repository.getStationCount()
            Log.d("StationDataImporter", "📊 Current station count: $existingCount")

            if (existingCount > 0) {
                Log.d("StationDataImporter", "🔄 Clearing existing data for fresh import")
                repository.deleteAllStations()
            }

            val stationData = mutableListOf<Triple<Int, String, String>>()
            Log.d("StationDataImporter", "📂 Opening station_data.csv from assets")

            val inputStream: InputStream = assetManager.open("station_data.csv")
            csvReader().open(inputStream) {
                readAllAsSequence().forEachIndexed { index, row ->
                    if (index == 0) {
                        Log.d("StationDataImporter", "📋 Skipping header row: ${row.joinToString(",")}")
                        return@forEachIndexed // Skip header
                    }

                    if (row.size >= 3) {
                        val buildingNumber = row[0].trim().toIntOrNull() ?: 3
                        val stationNumber = row[1].trim()
                        val checkDigit = row[2].trim()
                        stationData.add(Triple(buildingNumber, stationNumber, checkDigit))

                        // Enhanced logging for debugging
                        if (index <= 5 || index % 50 == 0) {
                            Log.d("StationDataImporter", "📝 Row $index: Building $buildingNumber, '$stationNumber' -> '$checkDigit'")
                        }
                    } else {
                        Log.w("StationDataImporter", "⚠️ Skipping malformed row $index: ${row.joinToString(",")}")
                    }
                }
            }

            Log.d("StationDataImporter", "✅ Finished reading CSV. Total valid rows: ${stationData.size}")

            if (stationData.isEmpty()) {
                Log.e("StationDataImporter", "❌ No valid station data found in CSV")
                return Result.failure(Exception("No valid station data found in CSV file"))
            }

            Log.d("StationDataImporter", "💾 Importing ${stationData.size} stations to database")
            val importResult = repository.importStations(stationData)

            importResult.onSuccess { count ->
                Log.d("StationDataImporter", "🎉 Successfully imported $count stations!")
            }.onFailure { error ->
                Log.e("StationDataImporter", "❌ Failed to import stations: ${error.message}", error)
            }

            return importResult

        } catch (e: Exception) {
            Log.e("StationDataImporter", "❌ Import failed with exception: ${e.message}", e)
            return Result.failure(e)
        }
    }

    /**
     * Import additional stations for testing
     */
    suspend fun importTestData(): Result<Int> {
        val testStations = listOf(
            Triple(3, "58-15", "69"), // Building 3, Station 58-15
            Triple(3, "58-16", "90"),
            Triple(3, "57-30", "45"),
            Triple(2, "40-01", "11"), // Building 2 test data
            Triple(4, "40-01", "22")  // Building 4 test data
        )

        return repository.importStations(testStations)
    }
}
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

            val stationData = mutableListOf<Pair<String, String>>()
            Log.d("StationDataImporter", "📂 Opening station_data.csv from assets")

            val inputStream: InputStream = assetManager.open("station_data.csv")
            csvReader().open(inputStream) {
                readAllAsSequence().forEachIndexed { index, row ->
                    if (index == 0) {
                        Log.d("StationDataImporter", "📋 Skipping header row: ${row.joinToString(",")}")
                        return@forEachIndexed // Skip header
                    }

                    if (row.size >= 2) {
                        val stationNumber = row[0].trim()
                        val checkDigit = row[1].trim()
                        stationData.add(stationNumber to checkDigit)

                        // Enhanced logging for debugging
                        if (index <= 5 || index % 50 == 0) {
                            Log.d("StationDataImporter", "📝 Row $index: '$stationNumber' -> '$checkDigit'")
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
            "03-58-15-02" to "99", // Your Old Roy example (position 2)
            "03-58-16-02" to "01",
            "03-57-30-02" to "45",
            "03-57-31-02" to "46"
        )

        return repository.importStations(testStations)
    }
}
package com.dollargeneral.palletmanager.data.repository

import android.util.Log
import com.dollargeneral.palletmanager.data.dao.PalletAssignmentDao
import com.dollargeneral.palletmanager.data.dao.StationCheckDigitDao
import com.dollargeneral.palletmanager.data.database.StationUtils
import com.dollargeneral.palletmanager.data.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing pallet assignments and station check digits
 * Provides a clean API for the UI layer and handles business logic
 */
@Singleton
class PalletRepository @Inject constructor(
    private val palletAssignmentDao: PalletAssignmentDao,
    private val stationCheckDigitDao: StationCheckDigitDao
) {
    
    // ========== Pallet Assignment Operations ==========
    
    /**
     * Get all active pallet assignments
     * Returns Flow for real-time UI updates
     */
    fun getActiveAssignments(): Flow<List<ActiveAssignment>> {
        return palletAssignmentDao.getActiveAssignments()
    }
    
    /**
     * Add a new pallet assignment
     * Automatically normalizes the destination format and increments station usage
     */
    suspend fun addAssignment(
        productName: String,
        destination: String,
        checkDigit: String,
        notes: String = ""
    ): Result<Long> {
        return try {
            val normalizedDestination = StationUtils.normalizeStationNumber(destination)
            
            val assignment = PalletAssignment(
                productName = productName.trim(),
                destination = normalizedDestination,
                checkDigit = checkDigit.trim(),
                notes = notes.trim(),
                createdAt = Date()
            )
            
            val id = palletAssignmentDao.insertAssignment(assignment)
            
            // Increment usage frequency for this station
            stationCheckDigitDao.incrementUsage(normalizedDestination)
            
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark a pallet as delivered
     */
    suspend fun markAsDelivered(assignmentId: Long): Result<Unit> {
        return try {
            palletAssignmentDao.markAsDelivered(assignmentId, Date())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete an assignment
     */
    suspend fun deleteAssignment(assignmentId: Long): Result<Unit> {
        return try {
            palletAssignmentDao.deleteAssignment(assignmentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get delivery history
     */
    suspend fun getDeliveryHistory(limit: Int = 50): List<PalletAssignment> {
        return palletAssignmentDao.getDeliveryHistory(limit)
    }
    
    /**
     * Get count of active assignments
     */
    fun getActiveAssignmentCount(): Flow<Int> {
        return palletAssignmentDao.getActiveAssignmentCount()
    }
    
    // ========== Station Check Digit Operations ==========
    
    /**
     * Look up check digit for a destination in a specific building
     * Returns null if not found
     */
    suspend fun getCheckDigitForStation(buildingNumber: Int, destination: String): String? {
        Log.d("PalletRepository", "🔍 lookupCheckDigit input: building=$buildingNumber, station='$destination'")
        val normalizedDestination = StationUtils.normalizeStationNumber(destination)
        Log.d("PalletRepository", "🔍 normalized to: '$normalizedDestination'")

        val result = stationCheckDigitDao.getCheckDigit(buildingNumber, normalizedDestination)
        Log.d("PalletRepository", "🔍 DAO result: '$result'")

        // Also check total count in database for debugging
        val totalCount = stationCheckDigitDao.getStationCount()
        Log.d("PalletRepository", "🔍 Total stations in DB: $totalCount")

        return result
    }
    
    /**
     * Add or update a station check digit
     */
    suspend fun addOrUpdateStation(
        buildingNumber: Int,
        stationNumber: String,
        checkDigit: String,
        description: String = ""
    ): Result<Unit> {
        return try {
            val normalizedStation = StationUtils.normalizeStationNumber(stationNumber)

            val station = StationCheckDigit(
                buildingNumber = buildingNumber,
                stationNumber = normalizedStation,
                checkDigit = checkDigit.trim(),
                description = description.trim(),
                lastUpdated = Date()
            )

            stationCheckDigitDao.insertOrUpdateStation(station)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all stations for a specific building
     */
    fun getAllStations(buildingNumber: Int): Flow<List<StationLookup>> {
        return stationCheckDigitDao.getAllStations(buildingNumber)
    }

    /**
     * Search stations in a specific building
     */
    suspend fun searchStations(buildingNumber: Int, searchTerm: String): List<StationLookup> {
        return stationCheckDigitDao.searchStations(buildingNumber, searchTerm)
    }
    
    /**
     * Delete a station
     */
    suspend fun deleteStation(buildingNumber: Int, stationNumber: String): Result<Unit> {
        return try {
            val normalizedStation = StationUtils.normalizeStationNumber(stationNumber)
            stationCheckDigitDao.deleteStation(buildingNumber, normalizedStation)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete all stations
     */
    suspend fun deleteAllStations(): Result<Unit> {
        return try {
            stationCheckDigitDao.deleteAllStations()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get most frequently used stations
     */
    suspend fun getMostUsedStations(limit: Int = 20): List<StationLookup> {
        return stationCheckDigitDao.getMostUsedStations(limit)
    }
    
    /**
     * Get stations by aisle (e.g., all stations in aisle 58) in a specific building
     */
    suspend fun getStationsByAisle(buildingNumber: Int, aisleNumber: String): List<StationLookup> {
        val pattern = "${aisleNumber.padStart(2, '0')}-%"
        return stationCheckDigitDao.getStationsByAisle(buildingNumber, pattern)
    }
    
    /**
     * Batch import stations from your recorded data
     * Useful for initial setup
     * Format: Triple(buildingNumber, stationNumber, checkDigit)
     */
    suspend fun importStations(stations: List<Triple<Int, String, String>>): Result<Int> {
        Log.d("PalletRepository", "Received ${stations.size} stations for import.")
        return try {
            val stationEntities = stations.map { (buildingNumber, stationNumber, checkDigit) ->
                StationCheckDigit(
                    buildingNumber = buildingNumber,
                    stationNumber = StationUtils.normalizeStationNumber(stationNumber),
                    checkDigit = checkDigit.trim(),
                    lastUpdated = Date()
                )
            }
            
            val insertedCount = stationCheckDigitDao.insertStations(stationEntities)
            Log.d("PalletRepository", "Successfully inserted $insertedCount stations into DAO.")
            Result.success(insertedCount)
        } catch (e: Exception) {
            Log.e("PalletRepository", "Error importing stations: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get station count
     */
    suspend fun getStationCount(): Int {
        return stationCheckDigitDao.getStationCount()
    }

    /**
     * Get recently used stations (based on usage frequency)
     */
    suspend fun getRecentlyUsedStations(limit: Int = 10): List<StationLookup> {
        return stationCheckDigitDao.getMostUsedStations(limit)
    }

    /**
     * Record station usage for analytics and quick access
     */
    suspend fun recordStationUsage(stationNumber: String) {
        val normalizedStation = StationUtils.normalizeStationNumber(stationNumber)
        stationCheckDigitDao.incrementUsage(normalizedStation)
    }
    
    /**
     * Validate and suggest station format
     */
    fun validateAndFormatStation(input: String): Pair<Boolean, String> {
        val normalized = StationUtils.normalizeStationNumber(input)
        val isValid = StationUtils.isValidStationNumber(input)
        return Pair(isValid, normalized)
    }
    
    /**
     * Clean up old delivered assignments (older than 30 days)
     */
    suspend fun cleanupOldDeliveries(daysToKeep: Int = 30): Result<Unit> {
        return try {
            val cutoffDate = Date(System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L))
            palletAssignmentDao.cleanupOldDeliveries(cutoffDate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

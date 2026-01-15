package com.dollargeneral.palletmanager.data.dao

import android.util.Log
import androidx.room.*
import com.dollargeneral.palletmanager.data.entities.StationCheckDigit
import com.dollargeneral.palletmanager.data.entities.StationLookup
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for station check digits
 * Handles the lookup database for auto-filling check digits
 */
@Dao
interface StationCheckDigitDao {
    
    /**
     * Get check digit for a specific station in a specific building
     * Used for auto-filling when entering destinations
     */
    @Query("SELECT checkDigit FROM station_check_digits WHERE buildingNumber = :buildingNumber AND stationNumber = :stationNumber")
    suspend fun getCheckDigit(buildingNumber: Int, stationNumber: String): String?
    
    /**
     * Get all stations with their check digits for a specific building
     * Used for the station database management screen
     */
    @Query("""
        SELECT buildingNumber, stationNumber, checkDigit, description, usageFrequency
        FROM station_check_digits
        WHERE buildingNumber = :buildingNumber
        ORDER BY usageFrequency DESC, stationNumber ASC
    """)
    fun getAllStations(buildingNumber: Int): Flow<List<StationLookup>>
    
    /**
     * Search stations by station number or description in a specific building
     */
    @Query("""
        SELECT buildingNumber, stationNumber, checkDigit, description, usageFrequency
        FROM station_check_digits
        WHERE buildingNumber = :buildingNumber
          AND (stationNumber LIKE '%' || :searchTerm || '%'
           OR description LIKE '%' || :searchTerm || '%')
        ORDER BY usageFrequency DESC, stationNumber ASC
    """)
    suspend fun searchStations(buildingNumber: Int, searchTerm: String): List<StationLookup>
    
    /**
     * Insert or update a station check digit
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStation(station: StationCheckDigit)
    
    /**
     * Batch insert multiple stations
     * Useful for initial setup with your recorded check digits
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(stations: List<StationCheckDigit>): List<Long>

    /**
     * Batch insert multiple stations
     * Useful for initial setup with your recorded check digits
     */
    @Transaction
    suspend fun insertStations(stations: List<StationCheckDigit>): Int {
        Log.d("StationCheckDigitDao", "Attempting to insert ${stations.size} stations into database.")
        val insertedIds = insertAll(stations)
        val count = insertedIds.size
        Log.d("StationCheckDigitDao", "Successfully inserted $count stations.")
        return count
    }
    
    /**
     * Update a station's check digit
     */
    @Query("""
        UPDATE station_check_digits
        SET checkDigit = :checkDigit, lastUpdated = :lastUpdated
        WHERE buildingNumber = :buildingNumber AND stationNumber = :stationNumber
    """)
    suspend fun updateCheckDigit(
        buildingNumber: Int,
        stationNumber: String,
        checkDigit: String,
        lastUpdated: Date = Date()
    )
    
    /**
     * Update station description
     */
    @Query("""
        UPDATE station_check_digits 
        SET description = :description, lastUpdated = :lastUpdated 
        WHERE stationNumber = :stationNumber
    """)
    suspend fun updateDescription(
        stationNumber: String, 
        description: String, 
        lastUpdated: Date = Date()
    )
    
    /**
     * Increment usage frequency when a station is used
     * Helps prioritize frequently used stations
     */
    @Query("""
        UPDATE station_check_digits 
        SET usageFrequency = usageFrequency + 1 
        WHERE stationNumber = :stationNumber
    """)
    suspend fun incrementUsage(stationNumber: String)

    /**
     * Get most used stations for quick access
     */
    @Query("""
        SELECT buildingNumber, stationNumber, checkDigit, description, usageFrequency
        FROM station_check_digits
        WHERE usageFrequency > 0
        ORDER BY usageFrequency DESC
        LIMIT :limit
    """)
    suspend fun getMostUsedStations(limit: Int): List<StationLookup>

    /**
     * Delete a station
     */
    @Query("DELETE FROM station_check_digits WHERE buildingNumber = :buildingNumber AND stationNumber = :stationNumber")
    suspend fun deleteStation(buildingNumber: Int, stationNumber: String)

    /**
     * Delete all stations
     */
    @Query("DELETE FROM station_check_digits")
    suspend fun deleteAllStations()
    
    /**
     * Get stations by aisle range (e.g., all stations in aisle 58) in a specific building
     */
    @Query("""
        SELECT buildingNumber, stationNumber, checkDigit, description, usageFrequency
        FROM station_check_digits
        WHERE buildingNumber = :buildingNumber AND stationNumber LIKE :aislePattern
        ORDER BY stationNumber ASC
    """)
    suspend fun getStationsByAisle(buildingNumber: Int, aislePattern: String): List<StationLookup>
    

    
    /**
     * Get total count of stored stations
     */
    @Query("SELECT COUNT(*) FROM station_check_digits")
    suspend fun getStationCount(): Int
    
    /**
     * Check if a station exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM station_check_digits WHERE buildingNumber = :buildingNumber AND stationNumber = :stationNumber)")
    suspend fun stationExists(buildingNumber: Int, stationNumber: String): Boolean
}

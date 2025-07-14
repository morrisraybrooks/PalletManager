package com.dollargeneral.palletmanager.data.dao

import androidx.room.*
import com.dollargeneral.palletmanager.data.entities.PalletAssignment
import com.dollargeneral.palletmanager.data.entities.ActiveAssignment
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for pallet assignments
 * Handles all database operations for managing pallet deliveries
 */
@Dao
interface PalletAssignmentDao {
    
    /**
     * Get all active (undelivered) pallet assignments
     * Returns a Flow for real-time UI updates
     */
    @Query("""
        SELECT id, productName, destination, checkDigit, createdAt, notes 
        FROM pallet_assignments 
        WHERE isDelivered = 0 
        ORDER BY createdAt ASC
    """)
    fun getActiveAssignments(): Flow<List<ActiveAssignment>>
    
    /**
     * Get a specific pallet assignment by ID
     */
    @Query("SELECT * FROM pallet_assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Long): PalletAssignment?
    
    /**
     * Insert a new pallet assignment
     * Returns the ID of the newly inserted assignment
     */
    @Insert
    suspend fun insertAssignment(assignment: PalletAssignment): Long
    
    /**
     * Update an existing pallet assignment
     */
    @Update
    suspend fun updateAssignment(assignment: PalletAssignment)
    
    /**
     * Mark a pallet as delivered
     */
    @Query("""
        UPDATE pallet_assignments 
        SET isDelivered = 1, deliveredAt = :deliveredAt 
        WHERE id = :id
    """)
    suspend fun markAsDelivered(id: Long, deliveredAt: Date = Date())
    
    /**
     * Delete a specific assignment
     */
    @Query("DELETE FROM pallet_assignments WHERE id = :id")
    suspend fun deleteAssignment(id: Long)
    
    /**
     * Get delivery history (delivered pallets)
     * Useful for tracking daily productivity
     */
    @Query("""
        SELECT * FROM pallet_assignments 
        WHERE isDelivered = 1 
        ORDER BY deliveredAt DESC 
        LIMIT :limit
    """)
    suspend fun getDeliveryHistory(limit: Int = 50): List<PalletAssignment>
    
    /**
     * Get count of active assignments
     */
    @Query("SELECT COUNT(*) FROM pallet_assignments WHERE isDelivered = 0")
    fun getActiveAssignmentCount(): Flow<Int>
    
    /**
     * Clean up old delivered assignments (older than specified days)
     * Helps keep the database size manageable
     */
    @Query("""
        DELETE FROM pallet_assignments 
        WHERE isDelivered = 1 
        AND deliveredAt < :cutoffDate
    """)
    suspend fun cleanupOldDeliveries(cutoffDate: Date)
    
    /**
     * Search assignments by destination or product name
     */
    @Query("""
        SELECT * FROM pallet_assignments 
        WHERE (destination LIKE '%' || :searchTerm || '%' 
               OR productName LIKE '%' || :searchTerm || '%')
        AND isDelivered = 0
        ORDER BY createdAt ASC
    """)
    suspend fun searchActiveAssignments(searchTerm: String): List<PalletAssignment>
}

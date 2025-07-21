package com.dollargeneral.palletmanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a pallet assignment from the forklift computer
 * This stores the details of pallets that need to be delivered
 */
@Entity(tableName = "pallet_assignments")
data class PalletAssignment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * Product name (e.g., "Gravy Train", "Old Roy")
     * Optional field for reference
     */
    val productName: String = "",
    
    /**
     * Destination location in format X-XX-XX-X (e.g., "3-58-15-1")
     * This is normalized to XX-XX-XX-XX format in the database
     */
    val destination: String,
    
    /**
     * Check digit for the destination (e.g., "21", "99")
     * Two-digit number required for forklift computer entry
     */
    val checkDigit: String,
    
    /**
     * When this assignment was created
     */
    val createdAt: Date = Date(),
    
    /**
     * Whether this pallet has been delivered
     * Used to track completion and clean up the active list
     */
    val isDelivered: Boolean = false,
    
    /**
     * When this pallet was marked as delivered (if applicable)
     */
    val deliveredAt: Date? = null,
    
    /**
     * Optional notes about this assignment
     */
    val notes: String = ""
)

/**
 * Entity for storing check digits for warehouse stations
 * This is the lookup database for auto-filling check digits
 */
@Entity(tableName = "station_check_digits")
data class StationCheckDigit(
    @PrimaryKey
    val stationNumber: String, // Format: "03-58-15-01" (normalized)
    
    /**
     * The check digit for this station (e.g., "21", "99")
     */
    val checkDigit: String,
    
    /**
     * When this check digit was added/last updated
     */
    val lastUpdated: Date = Date(),
    
    /**
     * Optional description of what's typically stored at this station
     * (e.g., "Dog Food Section", "Cleaning Supplies")
     */
    val description: String = "",
    
    /**
     * How frequently this station is used (for sorting/prioritizing)
     * Higher numbers = more frequently used
     */
    val usageFrequency: Int = 0
)

/**
 * Data class for displaying active assignments in the UI
 */
data class ActiveAssignment(
    val id: Long,
    val productName: String,
    val destination: String,
    val checkDigit: String,
    val createdAt: Date,
    val notes: String
)

/**
 * Data class for station lookup results
 */
data class StationLookup(
    val stationNumber: String,
    val checkDigit: String,
    val description: String,
    val usageFrequency: Int
)

package com.dollargeneral.palletmanager.data.database

import android.util.Log
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dollargeneral.palletmanager.data.dao.PalletAssignmentDao
import com.dollargeneral.palletmanager.data.dao.StationCheckDigitDao
import com.dollargeneral.palletmanager.data.entities.PalletAssignment
import com.dollargeneral.palletmanager.data.entities.StationCheckDigit
import java.util.Date

/**
 * Type converters for Room database
 * Handles conversion between complex types and database-storable types
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

/**
 * Main Room database for PalletManager app
 * Stores pallet assignments and station check digits offline
 */
@Database(
    entities = [PalletAssignment::class, StationCheckDigit::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PalletManagerDatabase : RoomDatabase() {
    
    abstract fun palletAssignmentDao(): PalletAssignmentDao
    abstract fun stationCheckDigitDao(): StationCheckDigitDao
    
    companion object {
        const val DATABASE_NAME = "pallet_manager_database"
        
        /**
         * Pre-populate database with common station check digits
         * This includes the ranges you mentioned: 03-57-XX-01 and 03-58-XX-01
         */
        val CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database will be populated by the repository when first accessed
            }
        }
    }
}

/**
 * Utility functions for station number formatting
 */
object StationUtils {
    
    /**
     * Simple station number formatting for warehouse use
     * Converts various formats to XX-XX format for database lookup:
     * - "5801" -> "58-01"
     * - "58-01" -> "58-01"
     * - "3-58-01-1" -> "58-01"
     * - "03-58-01-01" -> "58-01"
     */
    fun normalizeStationNumber(input: String): String {
        Log.d("StationUtils", "normalizeStationNumber: Input = $input")

        val cleaned = input.trim().replace(Regex("[^0-9-]"), "")

        // If it's already in XX-XX format, keep it
        if (cleaned.matches(Regex("\\d{2}-\\d{2}"))) {
            Log.d("StationUtils", "normalizeStationNumber: Already in XX-XX format = $cleaned")
            return cleaned
        }

        // If it's 4 digits, convert to XX-XX format (e.g., "5801" -> "58-01")
        if (cleaned.length == 4 && cleaned.all { it.isDigit() }) {
            val aisle = cleaned.substring(0, 2)
            val station = cleaned.substring(2, 4)
            val result = "$aisle-$station"
            Log.d("StationUtils", "normalizeStationNumber: Converted 4-digit $cleaned to $result")
            return result
        }

        // Handle full format: "3-58-01-1" or "03-58-01-01" -> "58-01"
        val fullFormatMatch = Regex("\\d{1,2}-(\\d{2})-(\\d{2})-\\d{1,2}").find(cleaned)
        if (fullFormatMatch != null) {
            val aisle = fullFormatMatch.groupValues[1]
            val station = fullFormatMatch.groupValues[2]
            val result = "$aisle-$station"
            Log.d("StationUtils", "normalizeStationNumber: Converted full format $cleaned to $result")
            return result
        }

        // Handle partial format: "3-58-01" -> "58-01"
        val partialFormatMatch = Regex("\\d{1,2}-(\\d{2})-(\\d{2})").find(cleaned)
        if (partialFormatMatch != null) {
            val aisle = partialFormatMatch.groupValues[1]
            val station = partialFormatMatch.groupValues[2]
            val result = "$aisle-$station"
            Log.d("StationUtils", "normalizeStationNumber: Converted partial format $cleaned to $result")
            return result
        }

        // Otherwise, return as-is
        Log.d("StationUtils", "normalizeStationNumber: No conversion applied = $cleaned")
        return cleaned
    }
    
    /**
     * Format station number for display (simplified format)
     * Converts "58-12" to "58-12" (already user-friendly)
     */
    fun formatForDisplay(stationNumber: String): String {
        val parts = stationNumber.split("-")
        if (parts.size == 2) {
            val aisle = parts[0].toIntOrNull()?.toString() ?: parts[0]
            val station = parts[1].toIntOrNull()?.toString() ?: parts[1]
            return "$aisle-$station"
        }
        return stationNumber
    }
    
    /**
     * Validate station number format (standard format: "58-01")
     */
    fun isValidStationNumber(input: String): Boolean {
        val cleaned = input.trim()

        // Check if it matches the standard format: XX-XX (like "58-01")
        return cleaned.matches(Regex("\\d{2}-\\d{2}"))
    }

    /**
     * Get validation status for all supported formats
     * Supports: "58-01", "5801", "3-58-01-1", "03-58-01-01"
     */
    fun getValidationStatus(input: String): ValidationResult {
        Log.d("StationUtils", "🟢 getValidationStatus called with: '$input'")

        if (input.isEmpty()) {
            Log.d("StationUtils", "🟢 Result: EMPTY")
            return ValidationResult.EMPTY
        }

        val result = when {
            // Complete formats that should trigger auto-lookup
            input.matches(Regex("\\d{2}-\\d{2}")) -> {
                Log.d("StationUtils", "🟢 Matched XX-XX format: '$input'")
                ValidationResult.VALID // "58-01"
            }
            input.length == 4 && input.all { it.isDigit() } -> {
                Log.d("StationUtils", "🟢 Matched 4-digit compact format: '$input'")
                ValidationResult.COMPACT_FORMAT // "5801"
            }
            input.matches(Regex("\\d{1,2}-\\d{2}-\\d{2}-\\d{1,2}")) -> {
                Log.d("StationUtils", "🟢 Matched full format: '$input'")
                ValidationResult.FULL_FORMAT // "3-58-01-1" or "03-58-01-01"
            }
            input.matches(Regex("\\d{1,2}-\\d{2}-\\d{2}")) -> {
                Log.d("StationUtils", "🟢 Matched partial full format: '$input'")
                ValidationResult.PARTIAL_FULL_FORMAT // "3-58-01" (missing position)
            }

            // Partial formats that should NOT trigger lookup
            input.length < 3 -> {
                Log.d("StationUtils", "🟢 Too short: '$input'")
                ValidationResult.TOO_SHORT // "5", "58"
            }
            input.matches(Regex("\\d{2}-\\d{1}")) -> {
                Log.d("StationUtils", "🟢 Partial format XX-X: '$input'")
                ValidationResult.PARTIAL_FORMAT // "58-1"
            }
            input.matches(Regex("\\d{1,2}-")) -> {
                Log.d("StationUtils", "🟢 Partial format with trailing dash: '$input'")
                ValidationResult.PARTIAL_FORMAT // "58-"
            }
            input.length == 3 && input.all { it.isDigit() } -> {
                Log.d("StationUtils", "🟢 Partial 3-digit format: '$input'")
                ValidationResult.PARTIAL_FORMAT // "580"
            }

            // Invalid formats
            input.any { !it.isDigit() && it != '-' } -> {
                Log.d("StationUtils", "🟢 Invalid characters: '$input'")
                ValidationResult.INVALID_CHARACTERS
            }
            else -> {
                Log.d("StationUtils", "🟢 Invalid format: '$input'")
                ValidationResult.INVALID_FORMAT
            }
        }

        Log.d("StationUtils", "🟢 Final result: ${result.name} (isValid=${result.isValid})")
        return result
    }

    /**
     * Auto-format input as user types for better UX
     */
    fun autoFormatInput(input: String): String {
        val cleaned = input.replace(Regex("[^0-9-]"), "")

        return when {
            // Auto-format compact input: 4015 -> 4-01-5
            cleaned.length == 4 && cleaned.all { it.isDigit() } -> {
                "${cleaned[0]}-${cleaned.substring(1, 3)}-${cleaned[3]}"
            }
            // Auto-format partial: 40-15 -> 3-40-15-1
            cleaned.matches(Regex("\\d{2}-\\d{2}")) -> {
                "3-$cleaned-1"
            }
            else -> cleaned
        }
    }

    /**
     * Get helpful suggestions based on input
     */
    fun getInputSuggestions(input: String): List<String> {
        val suggestions = mutableListOf<String>()

        when {
            input.isEmpty() -> {
                suggestions.addAll(listOf("3-40-15-1", "4015", "40-15"))
            }
            input.length == 1 && input.all { it.isDigit() } -> {
                suggestions.add("$input-40-15-1")
            }
            input.length == 2 && input.all { it.isDigit() } -> {
                suggestions.addAll(listOf("3-$input-15-1", "${input}15"))
            }
            input.matches(Regex("\\d-\\d{1,2}")) -> {
                val parts = input.split("-")
                if (parts[1].length == 1) {
                    suggestions.add("${parts[0]}-${parts[1].padStart(2, '0')}-15-1")
                } else {
                    suggestions.add("${parts[0]}-${parts[1]}-15-1")
                }
            }
        }

        return suggestions.take(3)
    }
}

/**
 * Validation result for station number input
 */
enum class ValidationResult(val message: String, val isValid: Boolean) {
    EMPTY("Enter station number", false),
    TOO_SHORT("Keep typing...", false),
    VALID("Valid station format ✓", true),
    COMPACT_FORMAT("Compact format detected", true),
    FULL_FORMAT("Full format detected ✓", true),  // "3-58-01-1" or "03-58-01-01"
    PARTIAL_FULL_FORMAT("Almost there...", false),  // "3-58-01" (missing position)
    PARTIAL_FORMAT("Keep typing...", false),
    TOO_LONG("Too many digits", false),
    INVALID_CHARACTERS("Use only numbers and dashes", false),
    INVALID_FORMAT("Try format: 58-01 or 5801", false)
}

/**
 * Extract aisle number from station
 * "03-58-15-01" -> "58"
 */
fun getAisleNumber(stationNumber: String): String? {
    val parts = stationNumber.split("-")
    return if (parts.size >= 2) parts[1] else null
}

package com.dollargeneral.palletmanager

import com.dollargeneral.palletmanager.data.dao.StationCheckDigitDao
import com.dollargeneral.palletmanager.data.entities.StationCheckDigit
import com.dollargeneral.palletmanager.data.repository.PalletRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.robolectric.RobolectricTestRunner

/**
 * Test to verify real-time synchronization between station database operations
 * and UI components across the app
 */
@RunWith(RobolectricTestRunner::class)
class RealTimeSyncTest {

    private val mockDao = mock<StationCheckDigitDao>()
    private val repository = PalletRepository(mock(), mockDao)

    @Test
    fun `test reactive check digit lookup updates when station is added`() = runTest {
        // Given: Initially no station exists
        val stationNumber = "03-57-15-01"
        whenever(mockDao.getCheckDigitFlow(stationNumber)).thenReturn(
            flowOf(null, "42") // First null, then "42" after adding
        )

        // When: We observe the check digit flow
        val checkDigitFlow = repository.getCheckDigitForStationFlow(stationNumber)
        
        // Then: The flow should emit the updated check digit
        val checkDigits = mutableListOf<String?>()
        checkDigitFlow.collect { checkDigits.add(it) }
        
        // Verify we get both null and then the actual check digit
        assertEquals(2, checkDigits.size)
        assertNull(checkDigits[0])
        assertEquals("42", checkDigits[1])
    }

    @Test
    fun `test all stations flow updates when bulk stations are added`() = runTest {
        // Given: Initial empty list, then list with new stations
        val initialStations = emptyList<com.dollargeneral.palletmanager.data.entities.StationLookup>()
        val newStations = listOf(
            com.dollargeneral.palletmanager.data.entities.StationLookup(
                stationNumber = "03-57-01-01",
                checkDigit = "23",
                description = "",
                usageFrequency = 0
            ),
            com.dollargeneral.palletmanager.data.entities.StationLookup(
                stationNumber = "03-57-02-01", 
                checkDigit = "59",
                description = "",
                usageFrequency = 0
            )
        )
        
        whenever(mockDao.getAllStations()).thenReturn(
            flowOf(initialStations, newStations)
        )

        // When: We observe the all stations flow
        val allStationsFlow = repository.getAllStationsFlow()
        
        // Then: The flow should emit updated station list
        val stationLists = mutableListOf<List<com.dollargeneral.palletmanager.data.entities.StationLookup>>()
        allStationsFlow.collect { stationLists.add(it) }
        
        // Verify we get both empty list and then the populated list
        assertEquals(2, stationLists.size)
        assertEquals(0, stationLists[0].size)
        assertEquals(2, stationLists[1].size)
        assertEquals("03-57-01-01", stationLists[1][0].stationNumber)
        assertEquals("23", stationLists[1][0].checkDigit)
    }
}

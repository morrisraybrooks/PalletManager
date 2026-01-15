package com.dollargeneral.palletmanager.di

import android.content.Context
import androidx.room.Room
import com.dollargeneral.palletmanager.data.dao.PalletAssignmentDao
import com.dollargeneral.palletmanager.data.dao.StationCheckDigitDao
import com.dollargeneral.palletmanager.data.database.PalletManagerDatabase
import com.dollargeneral.palletmanager.data.importer.StationDataImporter
import com.dollargeneral.palletmanager.data.repository.PalletRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the Room database instance
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PalletManagerDatabase {
        return Room.databaseBuilder(
            context,
            PalletManagerDatabase::class.java,
            PalletManagerDatabase.DATABASE_NAME
        )
        .addCallback(PalletManagerDatabase.CALLBACK)
        .addMigrations(PalletManagerDatabase.MIGRATION_2_3)
        .fallbackToDestructiveMigration() // Clear and recreate database if migration fails
        .build()
    }
    
    /**
     * Provides the PalletAssignmentDao
     */
    @Provides
    fun providePalletAssignmentDao(database: PalletManagerDatabase): PalletAssignmentDao {
        return database.palletAssignmentDao()
    }
    
    /**
     * Provides the StationCheckDigitDao
     */
    @Provides
    fun provideStationCheckDigitDao(database: PalletManagerDatabase): StationCheckDigitDao {
        return database.stationCheckDigitDao()
    }

    /**
     * Provides the StationDataImporter
     */
    @Provides
    @Singleton
    fun provideStationDataImporter(
        repository: PalletRepository,
        @ApplicationContext context: Context
    ): StationDataImporter {
        return StationDataImporter(repository, context.assets)
    }
}

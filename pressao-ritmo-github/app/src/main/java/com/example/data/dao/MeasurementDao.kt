package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.MeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    // Profile-scoped queries
    @Query("SELECT * FROM measurements WHERE profileId = :profileId ORDER BY measuredAtEpoch DESC")
    fun getMeasurementsForProfile(profileId: Long): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE profileId = :profileId ORDER BY measuredAtEpoch DESC")
    suspend fun getMeasurementsForProfileDirect(profileId: Long): List<MeasurementEntity>

    @Query("SELECT * FROM measurements WHERE profileId = :profileId ORDER BY measuredAtEpoch DESC LIMIT :limit")
    fun getRecentMeasurementsForProfile(profileId: Long, limit: Int = 5): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE profileId = :profileId AND measuredAtEpoch >= :startEpoch AND measuredAtEpoch <= :endEpoch ORDER BY measuredAtEpoch ASC")
    fun getMeasurementsBetweenForProfile(profileId: Long, startEpoch: Long, endEpoch: Long): Flow<List<MeasurementEntity>>

    @Query("SELECT COUNT(*) FROM measurements WHERE profileId = :profileId")
    fun getMeasurementCountForProfile(profileId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM measurements WHERE profileId = :profileId")
    suspend fun getMeasurementCountForProfileDirect(profileId: Long): Int

    @Query("DELETE FROM measurements WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: Long)

    // Global queries
    @Query("SELECT * FROM measurements ORDER BY measuredAtEpoch DESC")
    fun getAllMeasurements(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements ORDER BY measuredAtEpoch DESC")
    suspend fun getAllMeasurementsDirect(): List<MeasurementEntity>

    @Query("SELECT * FROM measurements ORDER BY measuredAtEpoch DESC LIMIT :limit")
    fun getRecentMeasurements(limit: Int = 5): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE id = :id LIMIT 1")
    suspend fun getMeasurementById(id: String): MeasurementEntity?

    @Query("SELECT * FROM measurements WHERE measuredAtEpoch >= :startEpoch AND measuredAtEpoch <= :endEpoch ORDER BY measuredAtEpoch ASC")
    fun getMeasurementsBetween(startEpoch: Long, endEpoch: Long): Flow<List<MeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: MeasurementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<MeasurementEntity>)

    @Update
    suspend fun update(measurement: MeasurementEntity)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM measurements")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(measurements: List<MeasurementEntity>) {
        deleteAll()
        insertAll(measurements)
    }

    @Transaction
    suspend fun replaceProfileMeasurements(profileId: Long, measurements: List<MeasurementEntity>) {
        deleteByProfileId(profileId)
        insertAll(measurements)
    }
}


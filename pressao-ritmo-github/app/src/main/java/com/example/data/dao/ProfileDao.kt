package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile ORDER BY isDefault DESC, createdAt ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profile ORDER BY isDefault DESC, createdAt ASC")
    suspend fun getAllProfilesDirect(): List<ProfileEntity>

    @Query("SELECT * FROM profile WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdDirect(id: Long): ProfileEntity?

    @Query("SELECT * FROM profile ORDER BY isDefault DESC, id ASC LIMIT 1")
    fun getDefaultOrFirstProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile ORDER BY isDefault DESC, id ASC LIMIT 1")
    suspend fun getDefaultOrFirstProfileDirect(): ProfileEntity?

    @Query("SELECT COUNT(*) FROM profile")
    suspend fun countProfiles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(profiles: List<ProfileEntity>)

    @Update
    suspend fun update(profile: ProfileEntity)

    @Query("DELETE FROM profile WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM profile")
    suspend fun deleteAll()

    @Query("UPDATE profile SET isDefault = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE profile SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultProfile(id: Long)

    @Transaction
    suspend fun setAsDefault(id: Long) {
        clearDefaultFlags()
        setDefaultProfile(id)
    }

    // Backward compatibility helper
    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: ProfileEntity)
}


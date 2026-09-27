package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.BusinessProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(businessProfile: BusinessProfile): Long

    @Update
    suspend fun update(businessProfile: BusinessProfile)

    @Query("SELECT * FROM business_profile WHERE id = :id")
    fun getById(id: Long): Flow<BusinessProfile?>

    @Query("SELECT * FROM business_profile WHERE id = :id")
    suspend fun getByIdDirect(id: Long): BusinessProfile?

    @Query("SELECT * FROM business_profile WHERE isActive = 1")
    fun getAllActive(): Flow<List<BusinessProfile>>

    @Query("SELECT * FROM business_profile WHERE isActive = 1")
    suspend fun getAllActiveDirect(): List<BusinessProfile>

    @Query("UPDATE business_profile SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("DELETE FROM business_profile WHERE id = :id")
    suspend fun delete(id: Long)
}

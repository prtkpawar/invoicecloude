package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AppSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT value FROM settings WHERE `key` = :key AND businessId = :businessId LIMIT 1")
    fun getSettingFlow(key: String, businessId: Long = 1L): Flow<String?>

    @Query("SELECT value FROM settings WHERE `key` = :key AND businessId = :businessId LIMIT 1")
    suspend fun getSetting(key: String, businessId: Long = 1L): String?

    @Query("SELECT * FROM settings WHERE businessId = :businessId")
    fun getAllSettingsFlow(businessId: Long = 1L): Flow<List<AppSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<AppSetting>)
}

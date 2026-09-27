package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Company
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM company WHERE id = 1 LIMIT 1")
    fun getCompanyFlow(): Flow<Company?>

    @Query("SELECT * FROM company WHERE id = 1 LIMIT 1")
    suspend fun getCompany(): Company?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(company: Company)

    @Update
    suspend fun update(company: Company)
}

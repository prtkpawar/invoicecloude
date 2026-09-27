package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settings",
    indices = [
        Index("businessId")
    ]
)
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String,
    val businessId: Long = 1L
)

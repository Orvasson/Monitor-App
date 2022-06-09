package com.pentechnologies.wareader2.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HeartRate(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "heart_rate") val heart_rate: String?,
    @ColumnInfo(name = "rr_interval") val rr_interval: String?,
    @ColumnInfo(name = "timestamp") val timestamp: String?,
    @ColumnInfo(name = "date") val date: String?
)
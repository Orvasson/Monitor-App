package com.pentechnologies.wareader2.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Gps(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "latitude") val latitude: String?,
    @ColumnInfo(name = "longitude") val longitude: String?,
    @ColumnInfo(name = "time") val time: String?,
    @ColumnInfo(name = "date") val date: String?
)
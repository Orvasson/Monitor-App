package com.pentechnologies.wareader2.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SpO2(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "SP02") val SP02: String?,
    @ColumnInfo(name = "time") val time: String?,
    @ColumnInfo(name = "date") val date: String?
)